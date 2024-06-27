package com.bhoper.service;

import com.bhoper.dto.RoomStatusUpdateRequest;
import com.bhoper.model.Booking;
import com.bhoper.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    private final RestTemplate restTemplate;

    public List<Booking> findAll() {
        return this.bookingRepository.findAll();
    }

    public Optional<Booking> findById(Long id) {
        return this.bookingRepository.findById(id);
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        Boolean isRoomAvailable = this.restTemplate.getForObject(
                "http://room-management-service/api/rooms/availability/%s"
                        .formatted(booking.getRoomId()), Boolean.class);
        if (Boolean.TRUE.equals(isRoomAvailable)) {
            booking.setActive(true);
            restTemplate.postForObject("http://room-management-service/api/rooms/reserve",
                    booking.getRoomId(), String.class);
            return this.bookingRepository.save(booking);
        } else {
            throw new RuntimeException("Room is not available for booking");
        }
    }

    @Transactional
    public Booking updateBooking(Long id, Booking updatedBooking) {
        Optional<Booking> bookingOptional = this.bookingRepository.findById(id);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            if (!Objects.equals(booking.getRoomId(), updatedBooking.getRoomId())) {
                Boolean isRoomAvailable = this.restTemplate.getForObject(
                        "http://room-management-service/api/rooms/availability/%s"
                                .formatted(updatedBooking.getRoomId()), Boolean.class);
                if (Boolean.TRUE.equals(isRoomAvailable)) {
                    booking.setRoomId(
                            updatedBooking.getRoomId());
                } else {
                    throw new RuntimeException("Room is not available for booking");
                }
            }
            booking.setCheckInDate(updatedBooking.getCheckInDate());
            booking.setCheckOutDate(updatedBooking.getCheckOutDate());
            booking.setCustomerId(updatedBooking.getCustomerId());

            // todo: check if email exists

            return booking;
        } else {
            throw new RuntimeException("Booking not found");
        }
    }

    public void cancelBooking(Long id) {
        Optional<Booking> bookingOptional = this.bookingRepository.findById(id);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            RoomStatusUpdateRequest request = new RoomStatusUpdateRequest("available");
            this.restTemplate.patchForObject("http://room-management-service/api/rooms/status/%s"
                    .formatted(id), request, Boolean.class);
            this.bookingRepository.deleteById(id);
        } else {
            throw new RuntimeException("Booking not found");
        }
    }

}
