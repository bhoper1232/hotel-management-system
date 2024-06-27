package com.bhoper;

import com.bhoper.email.EmailSenderService;
import com.bhoper.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    @Autowired
    private EmailSenderService senderService;

    @KafkaListener(topics = "booking", groupId = "groupId")
    void listener(Booking data) {
        System.out.println(data);
        senderService.sendEmail(data.getEmail(), "Hotel management", "Your booking" +
                " was successfully placed\n Booking details:\n Your Room number: %s\n".formatted(data.getRoomId()));
    }
}
