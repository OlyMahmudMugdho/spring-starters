# Simplifying Kafka with Spring Boot 3 and Cloud Stream

In this article, we’ll craft a practical demo using **Spring Boot 3**, **Spring Cloud Stream**, and **Apache Kafka** to demonstrate a producer-consumer pattern. The app produces a single message on startup and consumes it from a Kafka topic, with an optional REST endpoint for manual triggering. We’ll explore the technologies, dissect the code, and provide thorough breakdowns.

## Introduction to Apache Kafka

**Apache Kafka** is a distributed streaming platform engineered for high-throughput, fault-tolerant, and real-time data processing. It’s a publish-subscribe messaging system where producers publish messages to topics, and consumers subscribe to process them. Kafka shines with:

- **Scalability**: Handles millions of messages per second across distributed brokers.
- **Durability**: Persists messages to disk with replication for reliability.
- **Partitioning**: Splits topics into partitions for parallel processing.
- **Consumer Groups**: Enables load-balanced consumption across multiple instances.

Kafka leverages **Zookeeper** for coordination and supports tools like the **Schema Registry** for schema management. Here, we’ll run Kafka locally via Docker on port `19092`.

## Introduction to Spring Cloud Stream

**Spring Cloud Stream** streamlines event-driven microservices by abstracting messaging systems like Kafka into a functional, declarative model. Built atop Spring Boot and Spring Integration, it provides:

- **Bindings**: Links application logic to message channels (e.g., Kafka topics).
- **Functions**: Uses `Supplier`, `Consumer`, or `Function` beans for message handling.
- **Binder**: Connects to messaging systems (we’ll use the Kafka binder).
- **Declarative Config**: Manages setup via properties, reducing code overhead.

In this demo, we’ll harness Spring Cloud Stream with Kafka for seamless message flow.

## Project Setup

Let’s build the project from the ground up, including Kafka infrastructure and the Spring Boot application.

### Kafka Infrastructure with Docker Compose

We’ll use Docker Compose to launch Kafka, Zookeeper, and Schema Registry locally. Save this as `docker-compose.yml`:

```yaml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181  # Client connection port
      ZOOKEEPER_TICK_TIME: 2000   # Heartbeat interval
    ports:
      - "2181:2181"  # Expose to host

  schema-registry:
    image: confluentinc/cp-schema-registry:latest
    hostname: schema-registry
    depends_on:
      - kafka-broker-1
    ports:
      - "8081:8081"  # Schema Registry port
    environment:
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL: 'zookeeper:2181'
      SCHEMA_REGISTRY_LISTENERS: http://schema-registry:8081
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: PLAINTEXT://kafka-broker-1:9092,PLAINTEXT_INTERNAL://localhost:19092
      SCHEMA_REGISTRY_DEBUG: 'true'

  kafka-broker-1:
    image: confluentinc/cp-kafka:latest
    hostname: kafka-broker-1
    ports:
      - "19092:19092"  # External port mapping
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-broker-1:9092,PLAINTEXT_INTERNAL://localhost:19092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    restart: always
```

- **Zookeeper**: Runs on `2181`, coordinating Kafka’s brokers and metadata.
- **Kafka Broker**: A single broker (ID: 1) with listeners on `9092` (internal) and `19092` (external, mapped to the host).
- **Schema Registry**: Listens on `8081`, connecting to Kafka and Zookeeper (optional here but included for extensibility).

Start it with:
```bash
docker-compose up -d
```

### Creating the Spring Boot Project

Visit [start.spring.io](https://start.spring.io/) to generate the project:
- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 3.4.3 
- **Group**: `com.mahmud`
- **Artifact**: `cloud-stream-demo`
- **Java Version**: 21 
- **Dependencies**:
    - Spring Web (for REST endpoints)
    - Spring Cloud Stream (core framework)
    - Lombok (for concise code)

Download the ZIP, extract it, and open it in your IDE.

### Adding the Kafka Binder Dependency

Spring Initializr includes Spring Web, Spring Cloud Stream, and Lombok, but we need to manually add the Kafka binder to connect Spring Cloud Stream to Kafka. Add this to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-kafka</artifactId>
</dependency>
``` 
connects Spring Cloud Stream to Kafka.

### Final Dependency Configuration

Here’s your complete `pom.xml` with the Kafka binder included:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.4.3</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.mahmud</groupId>
	<artifactId>cloud-stream-demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>cloud-stream-demo</name>
	<description>cloud-stream-demo</description>
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
	<properties>
		<java.version>21</java.version>
		<spring-cloud.version>2024.0.0</spring-cloud.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream-test-binder</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-stream-kafka</artifactId>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<scope>annotationProcessor</scope>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
</project>
```

- **Dependencies**:
    - `spring-boot-starter-web`: Enables REST capabilities.
    - `spring-cloud-stream`: Core Spring Cloud Stream framework.
    - `spring-cloud-starter-stream-kafka`: Kafka binder (manually added).
    - `lombok`: Reduces boilerplate with annotations.
    - Test dependencies (`spring-boot-starter-test`, `spring-cloud-stream-test-binder`) for unit testing.
- **Java Version**: Set to 21, matching your setup.
- **Spring Boot**: Version 3.4.3, as per your `pom.xml`.

Run `mvn clean install` to download dependencies.

---

## Project Overview

Our demo features:
- A Spring Boot app producing one `Greeting` message on startup and consuming it from Kafka.
- A Docker Compose setup for Kafka infrastructure.
- An optional REST endpoint for manual message production.

Directory structure:
```
.
├── docker-compose.yml
├── pom.xml
├── src
│   ├── main
│   │   ├── java/com/mahmud/cloudstreamdemo
│   │   │   ├── CloudStreamDemoApplication.java
│   │   │   ├── configs/KafkaStreamConfig.java
│   │   │   ├── controllers/MessageController.java
│   │   │   └── models/Greeting.java
│   │   └── resources/application.yaml
│   └── test/...
└── target/...
```

Let’s dive into the code!

---

## Code and Breakdowns

### 1. `application.yaml` - Configuration

Ever wondered how a few lines can tie Spring to Kafka? Here’s the glue that makes it happen:

```yaml
spring:
  application:
    name: cloud-stream-demo  # Unique name for the Spring application
  cloud:
    stream:
      bindings:
        produceMessage-out-0:  # Output binding for the producer
          destination: demo-topic  # Kafka topic to send messages to
        consumeMessage-in-0:   # Input binding for the consumer
          destination: demo-topic  # Kafka topic to consume messages from
      kafka:
        binder:
          brokers: localhost:19092  # Kafka broker address (mapped via Docker)
    function:
      definition: produceMessage;consumeMessage  # Functions to bind to channels
```

#### Breakdown
- **`spring.application.name`**: Names the app `cloud-stream-demo`, aiding logs and consumer groups.
- **`spring.cloud.stream.bindings`**:
    - `produceMessage-out-0`: Ties the `produceMessage` function to `demo-topic` output.
    - `consumeMessage-in-0`: Links `consumeMessage` to `demo-topic` input.
    - Naming follows `<functionName>-<in|out>-<index>`.
- **`spring.cloud.stream.kafka.binder.brokers`**: Targets `localhost:19092`, aligning with our Kafka setup.
- **`spring.cloud.function.definition`**: Registers `produceMessage` and `consumeMessage` for binding.

This file orchestrates the producer-consumer flow effortlessly.

---

### 2. `KafkaStreamConfig.java` - Producer and Consumer Logic

Ready to see the heart of our messaging system? Let’s define how messages flow:

```java
package com.mahmud.cloudstreamdemo.configs;

import com.mahmud.cloudstreamdemo.models.Greeting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Configuration class defining Kafka producer and consumer beans for Spring Cloud Stream.
 */
@Configuration
public class KafkaStreamConfig {
    // Flag to control production; ensures the supplier runs only once
    private boolean firstRun = true;

    /**
     * Producer bean that supplies messages to Kafka.
     * Uses a Supplier functional interface to generate Greeting objects.
     * Configured to produce only one message on startup by using the firstRun flag.
     */
    @Bean
    public Supplier<Greeting> produceMessage() {
        return () -> {
            if (firstRun) {  // Check if this is the first invocation
                firstRun = false;  // Disable further production after the first run
                Greeting greeting = new Greeting();
                greeting.setMessage("Hello from Spring Cloud Stream!");
                greeting.setSender("DemoApp");
                System.out.println("Producing: " + greeting);  // Log the produced message
                return greeting;  // Send this message to Kafka
            }
            return null;  // Return null to stop production after the first message
        };
    }

    /**
     * Consumer bean that processes messages from Kafka.
     * Uses a Consumer functional interface to handle incoming Greeting objects.
     */
    @Bean
    public Consumer<Greeting> consumeMessage() {
        return greeting -> System.out.println("Consumed: " + greeting.getMessage() + " from " + greeting.getSender());
        // Log the consumed message details
    }
}
```

#### Breakdown
- **Package and Imports**: Under `configs`, importing `Greeting` and functional interfaces.
- **`@Configuration`**: Defines Spring beans.
- **`firstRun` Flag**: Limits the `Supplier` to one message, avoiding infinite polling.
- **`produceMessage` Supplier**:
    - Produces a `Greeting` once, then returns `null` to stop.
    - Logs production for confirmation.
- **`consumeMessage` Consumer**:
    - Handles each `Greeting` from Kafka, logging its contents.

This class ensures a controlled message lifecycle.

---

### 3. `Greeting.java` - Message Model

What’s a message without a structure? Meet the simple yet effective `Greeting`:

```java
package com.mahmud.cloudstreamdemo.models;

import lombok.Data;

/**
 * Data model representing a greeting message sent via Kafka.
 */
@Data
public class Greeting {
    private String message;  // The content of the greeting
    private String sender;   // The sender of the greeting
}
```

#### Breakdown
- **Package**: In `models`, keeping it organized.
- **`@Data`**: Lombok generates getters, setters, and `toString`.
- **Fields**: `message` and `sender` form a JSON-serializable payload.

It’s minimal but fits the demo perfectly.

---

### 4. `MessageController.java` - Optional REST Endpoint

Want to take control of message production? Here’s a RESTful twist:

```java
package com.mahmud.cloudstreamdemo.controllers;

import com.mahmud.cloudstreamdemo.models.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller to manually trigger message production via HTTP requests.
 */
@RestController
public class MessageController {

    @Autowired
    private StreamBridge streamBridge;  // Spring Cloud Stream utility for dynamic message sending

    /**
     * Endpoint to send a Greeting message to Kafka via a POST request.
     * @param greeting The Greeting object received in the request body
     * @return Confirmation message
     */
    @PostMapping("/send")
    public String sendMessage(@RequestBody Greeting greeting) {
        streamBridge.send("produceMessage-out-0", greeting);  // Send the message to the output binding
        System.out.println("Producing: " + greeting);  // Log the action
        return "Message sent: " + greeting;  // Return a response to the client
    }
}
```

#### Breakdown
- **Package**: In `controllers`, separating REST logic.
- **`@RestController`**: Manages HTTP requests.
- **`StreamBridge`**: Sends messages dynamically to `produceMessage-out-0`.
- **`@PostMapping("/send")`**: Processes POST requests, sending `Greeting` objects to Kafka.

This adds an interactive layer to the demo.

---

### 5. `CloudStreamDemoApplication.java` - Main Application

Every journey starts somewhere—here’s the launchpad for our app:

```java
package com.mahmud.cloudstreamdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Spring Boot application.
 */
@SpringBootApplication
public class CloudStreamDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudStreamDemoApplication.class, args);  // Start the Spring Boot app
    }
}
```

#### Breakdown
- **Package**: Root `com.mahmud.cloudstreamdemo`.
- **`@SpringBootApplication`**: Enables auto-configuration and scanning.
- **`main`**: Launches the app, wiring all components.

It’s the foundation of our project.

---

## Running the Demo

1. **Start Kafka**:
   ```bash
   docker-compose up -d
   ```

2. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```
   Output:
   ```
   Producing: Greeting(message=Hello from Spring Cloud Stream!, sender=DemoApp)
   Consumed: Hello from Spring Cloud Stream! from DemoApp
   ```

3. **Optional REST Test**:
   ```bash
   curl -X POST http://localhost:8080/send -H "Content-Type: application/json" -d '{"message":"Hi","sender":"User"}'
   ```

## Conclusion

This demo highlights Spring Boot 3 and Spring Cloud Stream’s ease in Kafka integration. A one-shot `Supplier` and a `Consumer` work in tandem, with a REST option for flexibility. Kafka’s power and Docker’s simplicity make it a joy to run. Next steps? Add error handling or scale with consumer groups!
