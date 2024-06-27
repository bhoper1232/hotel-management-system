package com.bhoper;

import com.bhoper.email.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {

    @Autowired
    private EmailSenderService senderService;

    @KafkaListener(topics = "email", groupId = "groupId")
    void listener(String data) {
        System.out.println(data);
        senderService.sendEmail(data, "Hotel management", "Your booking" +
                "was successfully placed");
    }
}
