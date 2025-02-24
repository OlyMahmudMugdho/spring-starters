package com.mahmud.kafkademo.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "my-topic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        System.out.println("Producing message: " + message);
        kafkaTemplate.send(TOPIC, message);
        // Alternatively, specify partition/key:
        // kafkaTemplate.send(TOPIC, 0, "key", message);
    }
}