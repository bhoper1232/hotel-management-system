package com.bhoper.service;

import com.bhoper.dto.RoomStatusUpdateRequest;
import com.bhoper.model.Room;
import com.bhoper.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> findAll() {
        return this.roomRepository.findAll();
    }

    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
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
