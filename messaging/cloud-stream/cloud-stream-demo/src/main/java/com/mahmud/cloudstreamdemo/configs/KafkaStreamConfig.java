package com.mahmud.cloudstreamdemo.configs;


import com.mahmud.cloudstreamdemo.models.Greeting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Configuration
public class KafkaStreamConfig {
    private boolean firstRun = true;

    // Producer: Supplies messages to Kafka
    // This code will produce messages infinitely,
    // so I am using  firstRun variable and conditions to stop after producing the first message
    @Bean
    public Supplier<Greeting> produceMessage() {
        return () -> {
            if (firstRun){
                firstRun = false;
                Greeting greeting = new Greeting();
                greeting.setMessage("Hello from Spring Cloud Stream!");
                greeting.setSender("DemoApp");
                System.out.println("Producing: " + greeting);
                return greeting;
            }
            return null; // stops after first message
        };
    }

    // Consumer: Consumes messages from Kafka
    @Bean
    public Consumer<Greeting> consumeMessage() {
        return greeting -> System.out.println("Consumed: " + greeting.getMessage() + " from " + greeting.getSender());
    }
}
