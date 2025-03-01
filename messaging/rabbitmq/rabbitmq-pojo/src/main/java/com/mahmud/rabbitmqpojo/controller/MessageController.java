package com.mahmud.rabbitmqpojo.controller;

import com.mahmud.rabbitmqpojo.dto.Message;
import com.mahmud.rabbitmqpojo.service.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private final MessageProducer messageProducer;

    @Autowired
    public MessageController(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestBody Message message) {
        // Send the message using the producer
        messageProducer.sendMessage(message);

        return "Message sent: " + message.getId()
                + " from " + message.getSender();
    }
}
