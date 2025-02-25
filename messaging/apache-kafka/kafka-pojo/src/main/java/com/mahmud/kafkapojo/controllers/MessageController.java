package com.mahmud.kafkapojo.controllers;

import com.mahmud.kafkapojo.models.UserMessage;
import com.mahmud.kafkapojo.services.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final KafkaProducerService producerService;

    public MessageController(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody UserMessage message) {
        producerService.sendUserMessage(message);
        return ResponseEntity.ok("Message sent successfully");
    }
}