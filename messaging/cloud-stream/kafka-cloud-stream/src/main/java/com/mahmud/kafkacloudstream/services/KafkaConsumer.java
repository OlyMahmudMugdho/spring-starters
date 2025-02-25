package com.mahmud.kafkacloudstream.services;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class KafkaConsumer {

    @Bean
    public Consumer<String> input() {
        return message -> {
            System.out.println("Message received: " + message);
        };
    }
}
