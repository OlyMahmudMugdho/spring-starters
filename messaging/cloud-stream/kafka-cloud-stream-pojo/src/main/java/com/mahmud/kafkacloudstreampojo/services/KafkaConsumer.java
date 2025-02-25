package com.mahmud.kafkacloudstreampojo.services;

import com.mahmud.kafkacloudstreampojo.models.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class KafkaConsumer {

    @Bean
    public Consumer<Message> input() {
        return message -> {
            System.out.println("Message received: " + message);
        };
    }
}