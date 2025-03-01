package com.mahmud.springrabbitmq.controller;

import com.mahmud.springrabbitmq.service.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private final MessageProducer messageProducer;

    @Autowired
    public MessageController(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }


    @GetMapping("/send")
    public String sendMessage(@RequestParam String message) {
        messageProducer.sendMessage(message);
        return "Message sent: " + message;
    }
}

