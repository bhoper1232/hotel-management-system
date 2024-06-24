package com.bhoper.controller;

import com.bhoper.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    

}
