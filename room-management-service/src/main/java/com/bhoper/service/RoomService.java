package com.bhoper.service;

import com.bhoper.dto.RoomStatusUpdateRequest;
import com.bhoper.model.Room;
import com.bhoper.repository.RoomRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    JedisPool pool = new JedisPool("localhost", 6379);

    private final Integer TTL = 500;

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Room> findAll() {
        return this.roomRepository.findAll();
    }

    public Optional<Room> getCachedRoom(Long id) {
        try (Jedis jedis = pool.getResource()) {
            String key = "room:%s".formatted(id);
            String raw = jedis.get(key);
            if (raw != null) {
                return Optional.ofNullable(mapper.readValue(raw, Room.class));
            }
            Optional<Room> roomOptional = this.roomRepository.findById(id);
            if (roomOptional.isEmpty()) {
                return Optional.empty();
            }
            Room room = roomOptional.get();
            jedis.setex(key, TTL, mapper.writeValueAsString(room));
            return roomOptional;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Room> findById(Long id) {
        return getCachedRoom(id);
    }

    public List<Room> findByStatus(String status) {
        return this.roomRepository.findByStatus(status);
    }

    public Room save(Room room) {
        return this.roomRepository.save(room);
    }

    public void deleteById(Long id) {
        this.roomRepository.deleteById(id);
    }

    public boolean isRoomAvailable(Long roomId) {
        Optional<Room> room = this.roomRepository.findById(roomId);
        return room.isPresent() && "available".equalsIgnoreCase(room.get().getStatus());
    }

    @Transactional
    public Boolean updateRoomStatus(Long roomId, RoomStatusUpdateRequest status) {
        Room room = this.roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setStatus(status.getStatus());
        return true;
    }

    public Room updateRoomDetails(Long id, Room roomDetails) {
        Room room = this.roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        boolean changes = false;
        if (!roomDetails.getType().equals(room.getType())) {
            changes = true;
            room.setType(roomDetails.getType());
        }
        if (roomDetails.getCapacity() != room.getCapacity()) {
            changes = true;
            room.setCapacity(roomDetails.getCapacity());
        }
        if (!roomDetails.getStatus().equals(room.getStatus())) {
            changes = true;
            room.setStatus(roomDetails.getStatus());
        }
        if (roomDetails.getPrice() != room.getPrice()) {
            changes = true;
            room.setPrice(roomDetails.getPrice());
        }

        if (!changes) throw new RuntimeException("No changes were found");

        return room;
    }
}
