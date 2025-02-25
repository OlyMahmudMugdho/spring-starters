package com.mahmud.kafkacloudstreampojo.controllers;

import com.mahmud.kafkacloudstreampojo.services.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private final KafkaProducer kafkaProducer;

    public MessageController(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @GetMapping("/send")
    public String sendMessage(@RequestParam String content) {
        kafkaProducer.sendMessage(content);
        return "Message sent to Kafka!";
    }
}