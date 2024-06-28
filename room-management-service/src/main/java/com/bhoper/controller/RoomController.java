package com.bhoper.controller;

import com.bhoper.dto.RoomStatusUpdateRequest;
import com.bhoper.model.Room;
import com.bhoper.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<Room> getAllRooms() {
        return this.roomService.findAll();
    }

    @GetMapping("{roomId}")
    public ResponseEntity<Room> findRoomById(@PathVariable("roomId") Long id) {
        Optional<Room> room = this.roomService.findById(id);
        return room.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/availability/{id}")
    public ResponseEntity<Boolean> isRoomAvailable(@PathVariable Long id) {
        boolean isAvailable = roomService.isRoomAvailable(id);
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("availability")
    public List<Room> getAvailableRooms() {
        return this.roomService.findByStatus("available");
    }

    @PostMapping("reserve")
    public ResponseEntity<String> reserveRoom(@RequestBody Long roomId) {
        if (this.roomService.isRoomAvailable(roomId)) {
            RoomStatusUpdateRequest request = new RoomStatusUpdateRequest("occupied");
            roomService.updateRoomStatus(roomId, request);
            return ResponseEntity.ok("Room served successfully");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Room is not available");
        }
    }

    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        return this.roomService.save(room);
    }

    @PatchMapping("status/{roomId}")
    public ResponseEntity<Boolean> updateRoomStatus(@PathVariable("roomId") Long id,
                                                    @RequestBody RoomStatusUpdateRequest
                                                            roomStatusUpdateRequest) {
        return ResponseEntity.ok(this.roomService.updateRoomStatus(id, roomStatusUpdateRequest));
    }

    @PatchMapping("{roomId}")
    @Transactional
    public ResponseEntity<Room> updateRoomDetails(@PathVariable("roomId") Long id,
                                           @RequestBody Room roomDetails) {
        return ResponseEntity.ok(this.roomService.updateRoomDetails(id, roomDetails));
    }

    @DeleteMapping("{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") Long id) {
        this.roomService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
