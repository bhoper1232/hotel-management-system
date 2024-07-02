package com.bhoper.service;

import com.bhoper.dto.RoomStatusUpdateRequest;
import com.bhoper.model.Booking;
import com.bhoper.repository.BookingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    private final JedisPool pool;

    @Value("${spring.data.redis.time-to-live}")
    private Integer TTL;

    private final ObjectMapper mapper = new ObjectMapper();

    private final RestTemplate restTemplate;

    public List<Booking> findAll() {
        return this.bookingRepository.findAll();
    }

    public Optional<Booking> getCachedBooking(Long id) {
        try (Jedis jedis = pool.getResource()) {
            String key = "booking:%s".formatted(id);
            String raw = jedis.get(key);
            if (raw != null) {
                return Optional.ofNullable(mapper.readValue(raw, Booking.class));
            }
            Optional<Booking> bookingOptional = this.bookingRepository.findById(id);
            if (bookingOptional.isEmpty()) {
                return Optional.empty();
            }
            Booking booking = bookingOptional.get();
            jedis.setex(key, TTL, mapper.writeValueAsString(booking));
            return bookingOptional;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Booking> findById(Long id) {
        return getCachedBooking(id);
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
        Booking booking = this.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

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
    }

    public void cancelBooking(Long id) {
        Booking booking = this.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        RoomStatusUpdateRequest request = new RoomStatusUpdateRequest("available");
        this.restTemplate.patchForObject("http://room-management-service/api/rooms/status/%s"
                .formatted(id), request, Boolean.class);
        this.bookingRepository.deleteById(id);
    }

}
