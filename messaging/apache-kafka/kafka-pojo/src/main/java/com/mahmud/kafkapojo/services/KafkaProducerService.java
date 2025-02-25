package com.mahmud.kafkapojo.services;

import com.mahmud.kafkapojo.models.UserMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "my-topic";

    private final KafkaTemplate<String, UserMessage> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, UserMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserMessage(UserMessage message) {
        kafkaTemplate.send(TOPIC, message.username(), message); // Send with key
    }
}