package com.mahmud.kafkacloudstream.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final StreamBridge streamBridge;

    public KafkaProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendMessage(String message) {
        // Send message to the "output" binding (my-topic)
        streamBridge.send("output", message);
        System.out.println("Message sent: " + message);
    }
}