package com.bhoper.controller;

import com.bhoper.model.Booking;
import com.bhoper.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public List<Booking> getAllBookings() {
        return this.bookingService.findAll();
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<Booking> getBookingById(@PathVariable("bookingId") Long id) {
        Optional<Booking> booking =  this.bookingService.findById(id);
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        try {
            Booking createdBooking = this.bookingService.createBooking(booking);
            return ResponseEntity.ok(createdBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<Booking> updateBooking(@PathVariable("bookingId") Long id,
                                                 @RequestBody Booking booking) {
        try {
            Booking updatedBooking = this.bookingService.updateBooking(id, booking);
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable("bookingId") Long id) {
        try {
            this.bookingService.cancelBooking(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
