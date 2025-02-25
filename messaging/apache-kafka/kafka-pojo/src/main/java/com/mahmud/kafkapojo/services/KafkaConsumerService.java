package com.mahmud.kafkapojo.services;

import com.mahmud.kafkapojo.models.UserMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "user-messages", groupId = "my-group")
    public void consume(UserMessage message) {
        System.out.printf("Received message from %s: %s%n",
                message.username(), message.content());
    }

    // To access headers/metadata:
    @KafkaListener(topics = "my-topic", groupId = "my-group")
    public void consumeWithMetadata(
            @Payload UserMessage message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {
        System.out.printf("Message from partition %d (key %s): %s%n",
                partition, key, message);
    }
}