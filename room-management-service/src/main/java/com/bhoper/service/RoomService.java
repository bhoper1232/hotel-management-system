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
        Optional<Room> room = this.roomRepository.findById(roomId);
        if (room.isPresent()) {
            Room existingRoom = room.get();
            existingRoom.setStatus(status.getStatus());
            return true;
        } else {
            throw new RuntimeException("Room not found");
        }
    }

    public Room updateRoomDetails(Long id, Room roomDetails) {
        Optional<Room> room = this.roomRepository.findById(id);
        if (room.isPresent()) {
            Room existingRoom = room.get();
            boolean changes = false;
            if (!roomDetails.getType().equals(existingRoom.getType())) {
                changes = true;
                existingRoom.setType(roomDetails.getType());
            }
            if (roomDetails.getCapacity() != existingRoom.getCapacity()) {
                changes = true;
                existingRoom.setCapacity(roomDetails.getCapacity());
            }
            if (!roomDetails.getStatus().equals(existingRoom.getStatus())) {
                changes = true;
                existingRoom.setStatus(roomDetails.getStatus());
            }
            if (roomDetails.getPrice() != existingRoom.getPrice()) {
                changes = true;
                existingRoom.setPrice(roomDetails.getPrice());
            }
            if (!changes) throw new RuntimeException("No changes were found");

            return existingRoom;
        } else {
            throw new RuntimeException("Room not found");
        }
    }
}
