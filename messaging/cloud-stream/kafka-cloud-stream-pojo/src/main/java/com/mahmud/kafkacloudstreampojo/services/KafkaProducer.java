package com.mahmud.kafkacloudstreampojo.services;

import com.mahmud.kafkacloudstreampojo.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final StreamBridge streamBridge;

    public KafkaProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendMessage(String content) {
        // Create a Message object
        Message message = new Message(content);

        // Send the Message object to Kafka
        streamBridge.send("output", message);
        System.out.println("Message sent: " + message);
    }
}
