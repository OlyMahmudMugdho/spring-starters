package com.mahmud.rabbitmqpojo.service;

import com.mahmud.rabbitmqpojo.dto.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    @RabbitListener(queues = "demo_queue")
    public void receiveMessage(Message message) {
        System.out.println("Received message: " +  message.getId() + " - Hello from " + message.getSender());
    }
}

