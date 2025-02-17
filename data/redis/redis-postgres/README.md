# Building a High-Performance Application with Redis and PostgreSQL in Spring Boot

---

## **Introduction**
In this article, we’ll explore how to build a Spring Boot application that integrates **Redis** for caching and **PostgreSQL** for persistent storage. By leveraging Redis's in-memory capabilities, we can significantly improve the performance of data retrieval while relying on PostgreSQL for durable storage. This guide will walk you through setting up Redis and PostgreSQL using Docker Compose, implementing the application, and testing its functionality.

---

## **Understanding Redis**

Redis (Remote Dictionary Server) is an open-source, in-memory data store that serves as a database, cache, and message broker. It is widely used for its speed and versatility, supporting various data structures like strings, hashes, lists, sets, and sorted sets. Redis operates entirely in memory, which makes it exceptionally fast for read and write operations.

---

## **Why Choose Redis?**

Redis is an excellent choice for applications that require high-speed data access. Its key advantages include:
- **Performance**: Redis performs operations in-memory, making it orders of magnitude faster than traditional disk-based databases.
- **Flexibility**: It supports multiple data structures, enabling use cases like caching, session management, real-time analytics, and more.
- **Scalability**: Redis can be used in distributed systems, making it ideal for modern microservices architectures.

---

## **What is Caching?**

Caching is the process of storing frequently accessed data in a high-speed storage layer (like Redis) to reduce the load on the primary database and improve response times. In this project, we’ll use Redis to cache user data fetched from PostgreSQL, ensuring faster access for subsequent requests.

---

## **Setting Up Redis Using Docker Compose**

To simplify the setup, we’ll use Docker Compose to run Redis. Below is the `docker-compose.yml` file for Redis:

```yaml
services:
  redis:
    image: redis/redis-stack-server:latest
    container_name: redis
    ports:
      - 6379:6379
```

Run the following command to start Redis:
```bash
docker-compose up -d
```

This will start a Redis server on `localhost:6379`.

---

## **Exploring Redis Data Structures**

Redis supports several data structures, including:
- **Strings**: Used to store simple key-value pairs.
- **Hashes**: Store objects as key-value maps (e.g., user sessions).
- **Lists**: Ordered collections of strings.
- **Sets**: Unordered collections of unique strings.
- **Sorted Sets**: Sets with scores for ranking (e.g., leaderboards).

---

## **Working with Redis CLI in a Docker Environment**

When Redis is running inside a Docker container, you can still interact with it using the Redis CLI. Here’s how:

1. **Access the Redis Container**:
   First, ensure your Redis container is running. You can verify this by listing all running containers:
   ```bash
   docker ps
   ```

   Look for the container named `redis`. If it’s running, proceed to the next step.

2. **Open a Terminal Inside the Redis Container**:
   Use the following command to open a terminal inside the Redis container:
   ```bash
   docker exec -it redis redis-cli
   ```

   This will launch the Redis CLI directly inside the container.

3. **Basic Redis Commands**:
   Once inside the Redis CLI, you can execute commands to interact with Redis. For example:
    - Set a key-value pair:
      ```bash
      SET mykey "Hello Redis"
      ```
    - Retrieve the value of a key:
      ```bash
      GET mykey
      ```
    - Delete a key:
      ```bash
      DEL mykey
      ```

4. **Using Hashes**:
   Since our application uses Redis hashes to store session data, you can experiment with hash commands:
    - Store a hash:
      ```bash
      HSET sessions:abc123 sessionId "abc123" userId "1" loginTime "2023-10-01T10:00:00" lastActiveTime "2023-10-01T10:05:00"
      ```
    - Retrieve a field from the hash:
      ```bash
      HGET sessions:abc123 userId
      ```

5. **Exiting the CLI**:
   To exit the Redis CLI, type:
   ```bash
   exit
   ```

By using these commands, you can inspect and manipulate the data stored in Redis during development or debugging.

---

## **Setting Up PostgreSQL Using Docker Compose**

Here’s the `docker-compose.yml` file for PostgreSQL:

```yaml
services:
  db:
    image: postgres
    container_name: postgres
    restart: always
    environment:
      POSTGRES_PASSWORD: mysecretpassword
    ports:
      - 5432:5432
    networks:
      - database
networks:
  database:
    driver: bridge
    name: database
version: '3.9'
```

Run the following command to start PostgreSQL:
```bash
docker-compose up -d
```

This will start a PostgreSQL instance on `localhost:5432`.

---

## **Project Overview**

We’ll build a Spring Boot application that integrates Redis and PostgreSQL to manage user data and sessions. The application provides REST APIs for performing CRUD operations on users and managing user sessions. Redis is used to cache user data and store session information, while PostgreSQL serves as the primary database.

---

## **Implementing the Application**

### **Dependencies**
The project uses the following dependencies in the `pom.xml` file:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.8.5</version>
    </dependency>
</dependencies>
```

- **spring-boot-starter-cache**: Enables caching functionality.
- **spring-boot-starter-data-jpa**: Provides JPA support for PostgreSQL.
- **spring-boot-starter-data-redis**: Integrates Redis for caching and session management.
- **spring-boot-starter-web**: Builds RESTful APIs.
- **postgresql**: PostgreSQL database driver.
- **lombok**: Reduces boilerplate code with annotations like `@Data`, `@NoArgsConstructor`, etc.
- **springdoc-openapi-starter-webmvc-ui**: Adds Swagger UI for API documentation.

---

### **Application Properties**
Below is the `application.properties` file with comments explaining each property:

```properties
# Application Name
spring.application.name=redis-postgres

# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb # Database URL
spring.datasource.username=postgres # Database username
spring.datasource.password=mysecretpassword # Database password
spring.jpa.hibernate.ddl-auto=update # Automatically updates the schema
spring.datasource.driver-class-name=org.postgresql.Driver # PostgreSQL driver

# Redis Configuration
spring.data.redis.host=localhost # Redis server host
spring.data.redis.port=6379 # Redis server port

# Swagger UI Custom Path
springdoc.swagger-ui.path=/swagger-ui.html # Swagger UI endpoint
```

### Redis Configuration Class

The `RedisConfig` class is a configuration class for setting up caching in a Spring application using Redis. It configures how data is cached in Redis, defines cache expiration, and specifies how cache values are serialized. Below is an explanation of the code:

#### `RedisConfig.java`
```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // Cache expiration time
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

### Code Breakdown:

### Annotations and Beans:
1. **`@Configuration`**: This annotation marks the class as a configuration class. It tells Spring to process the class and its annotations to define beans and configuration settings.

2. **`@EnableCaching`**: This enables Spring's caching mechanism, allowing Spring to automatically manage cache operations (e.g., caching method results) using Redis in this case.

3. **`@Bean`**: This annotation indicates that the `cacheConfiguration()` method will return a bean that should be managed by the Spring context. In this case, it returns a `RedisCacheConfiguration` object.

### `cacheConfiguration()` Method:

```java
@Bean
public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig() // Starts with the default cache configuration.
            .entryTtl(Duration.ofMinutes(10))  // Sets the cache entry's time-to-live (TTL) to 10 minutes.
            .disableCachingNullValues()  // Disables caching of null values to avoid storing unnecessary cache entries.
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));  // Specifies the value serialization format using JSON.
}
```

1. **`RedisCacheConfiguration.defaultCacheConfig()`**: This initializes the default cache configuration.

2. **`entryTtl(Duration.ofMinutes(10))`**: Configures the cache entry to expire after 10 minutes. After the TTL expires, the cached value is evicted from Redis.

3. **`disableCachingNullValues()`**: This disables caching for null values. This is useful as storing `null` in the cache would unnecessarily consume Redis memory.

4. **`serializeValuesWith(...)`**: Defines the serialization method for cache values. In this case, it uses `GenericJackson2JsonRedisSerializer`, which serializes objects to JSON format. This ensures that the cached data is human-readable and can be easily parsed.

### Purpose:
- The `RedisConfig` class configures caching with Redis, enabling automatic cache management for Spring beans and methods annotated with caching annotations (`@Cacheable`, `@CacheEvict`, etc.).
- It defines cache expiration policies (TTL) and how values are serialized before being stored in Redis.


### **Models**
#### **User Entity**
The `User` entity represents a user stored in PostgreSQL. Below is the code with comments:

```java
@Entity // Marks this class as a JPA entity mapped to the `_users` table.
@Data // Generates getters, setters, toString, equals, and hashCode methods.
@NoArgsConstructor // Generates a no-argument constructor.
@AllArgsConstructor // Generates a constructor with all arguments.
@Table(name = "_users") // Maps this entity to the `_users` table in PostgreSQL.
public class User {

    @Id // Marks this field as the primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the ID value.
    private Long id; // Unique identifier for the user.

    private String name; // Name of the user.
    private String email; // Email of the user.
}
```
The `User` class is a JPA entity mapped to the `_users` table in PostgreSQL. It uses Lombok annotations to automatically generate boilerplate code like getters, setters, and constructors. The `id` field is marked as the primary key, and its value is auto-generated by the database. The `name` and `email` fields store user data. This class simplifies working with the database, allowing you to easily save and retrieve user records.


#### **Session Entity**
The `Session` entity represents a user session stored in Redis. Below is the code with comments:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("sessions") // Maps this entity to a Redis hash named "sessions".
public class Session {

    @Id // Marks this field as the primary key in Redis.
    private String sessionId; // Unique identifier for the session.

    private Long userId; // Foreign key linking the session to a user in PostgreSQL.

    private LocalDateTime loginTime; // Timestamp when the session was created.

    private LocalDateTime lastActiveTime; // Timestamp of the last activity in the session.
}
```

The `Session` class represents a user session in Redis. It's mapped to a Redis hash named "sessions" using the `@RedisHash` annotation. The `sessionId` field is the primary key in Redis, uniquely identifying each session. The `userId` field acts as a foreign key, linking the session to a user in PostgreSQL. The `loginTime` and `lastActiveTime` fields store timestamps for when the session was created and the last activity occurred, respectively. Lombok annotations automatically generate constructors, getters, setters, and other utility methods for the class.

---

### **Repositories**
#### **UserRepository**
Provides CRUD operations for the `User` entity:

```java
@Repository // Marks this interface as a repository component in Spring.
public interface UserRepository extends JpaRepository<User, Long> {
    // Provides CRUD operations for the User entity.
    // The key type is Long (user ID), and the value type is User.
}
```

#### **SessionRepository**
Provides CRUD operations for the `Session` entity:

```java
@Repository // Marks this interface as a repository component in Spring.
public interface SessionRepository extends CrudRepository<Session, String> {
    // Provides CRUD operations for the Session entity.
    // The key type is String (session ID), and the value type is Session.
}
```

---

### **Service Layer**
### **UserService**
Handles user-related operations and integrates caching. Below is the code with comments:

```java
@Service // Marks this class as a service component in Spring.
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "users", key = "'allUsers'") // Caches the result under the key "users::allUsers".
    public List<User> getAllUsers() {
        System.out.println("Fetching users from the database...");
        return userRepository.findAll(); // Fetches all users from PostgreSQL.
    }

    @CacheEvict(value = "users", key = "'allUsers'") // Invalidates the cache after creating a user.
    public User createUser(User user) {
        return userRepository.save(user); // Saves the user to PostgreSQL.
    }

    @Cacheable(value = "users", key = "#id", condition = "#id > 10") // Caches the result only if the ID is greater than 10.
    public User getUserById(Long id) {
        System.out.println("Fetching user with ID " + id + " from the database...");
        return userRepository.findById(id).orElse(null);
    }

    @CachePut(value = "users", key = "#id") // Updates the cache with the result of this method.
    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        return userRepository.save(existingUser);
    }

    @CacheEvict(value = "users", key = "#id") // Removes the cache entry for the deleted user.
    public void deleteUser(Long id) {
        userRepository.deleteById(id); // Deletes the user from PostgreSQL.
    }
}
```

The `UserService` class is marked with the `@Service` annotation, indicating that it is a service component in a Spring application. It contains business logic related to user operations, leveraging the `UserRepository` for data access. It also utilizes Spring's caching annotations to optimize performance by caching user data in a Redis cache.

### Detailed Breakdown of the Methods:

1. **Constructor Injection:**
   ```java
   public UserService(UserRepository userRepository) {
       this.userRepository = userRepository;
   }
   ```
    - The constructor injects the `UserRepository` dependency, which provides access to the user data in PostgreSQL.

2. **`getAllUsers()` - Fetches all users and caches the result:**
   ```java
   @Cacheable(value = "users", key = "'allUsers'")
   public List<User> getAllUsers() {
       System.out.println("Fetching users from the database...");
       return userRepository.findAll();
   }
   ```
    - **@Cacheable**: This annotation indicates that the result of the `getAllUsers()` method will be cached under the key `"users::allUsers"`.
    - **Cache Behavior**: If the data has been cached, the cached result is returned directly. Otherwise, the method fetches all users from PostgreSQL and stores the result in the cache.
    - **Key**: The cache entry is stored under the key `"allUsers"`.

3. **`createUser()` - Saves a new user and evicts the cache:**
   ```java
   @CacheEvict(value = "users", key = "'allUsers'")
   public User createUser(User user) {
       return userRepository.save(user);
   }
   ```
    - **@CacheEvict**: This annotation ensures that the cache for the key `"allUsers"` is invalidated after a new user is created.
    - **Behavior**: After saving the user to the PostgreSQL database, the cache is cleared for the `"allUsers"` key, which ensures that subsequent calls will fetch fresh data.

4. **`getUserById()` - Fetches a user by ID and caches the result:**
   ```java
   @Cacheable(value = "users", key = "#id", condition = "#id > 10")
   public User getUserById(Long id) {
       System.out.println("Fetching user with ID " + id + " from the database...");
       return userRepository.findById(id).orElse(null);
   }
   ```
    - **@Cacheable**: The result of fetching a user by ID is cached with the key being the user ID (`#id`).
    - **Condition**: The cache is only used if the `id` is greater than 10, as specified by `condition = "#id > 10"`. This is useful for optimizing cache usage based on certain criteria.
    - **Behavior**: If the user with the specified `id` is not found in the cache, the method queries PostgreSQL for the user and stores the result in the cache.

5. **`updateUser()` - Updates an existing user and updates the cache:**
   ```java
   @CachePut(value = "users", key = "#id")
   public User updateUser(Long id, User updatedUser) {
       User existingUser = userRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
       existingUser.setName(updatedUser.getName());
       existingUser.setEmail(updatedUser.getEmail());
       return userRepository.save(existingUser);
   }
   ```
    - **@CachePut**: This annotation updates the cache with the result of the method. The cache entry for the user with the given ID (`#id`) will be updated with the latest user data.
    - **Behavior**: After finding the user, its details are updated, and the updated user is saved to PostgreSQL. The cache entry is also updated with the new user details.

6. **`deleteUser()` - Deletes a user and evicts the cache:**
   ```java
   @CacheEvict(value = "users", key = "#id")
   public void deleteUser(Long id) {
       userRepository.deleteById(id);
   }
   ```
    - **@CacheEvict**: This annotation removes the cache entry for the user with the specified ID (`#id`) after the user is deleted.
    - **Behavior**: After deleting the user from PostgreSQL, the cache for the deleted user is cleared.

### Caching Annotations Overview:
- **@Cacheable**: Caches the method result for the specified key. If the data is cached, it returns the cached value instead of executing the method.
- **@CacheEvict**: Removes the cache entry for a specified key, often used after performing write operations like creating, updating, or deleting.
- **@CachePut**: Updates the cache with the latest value after the method execution. This is useful for keeping the cache synchronized with the database.


### **SessionService**
Handles session-related operations:

```java
@Service // Marks this class as a service component in Spring.
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session createSession(Long userId) {
        Session session = new Session();
        session.setUserId(userId);
        session.setLoginTime(LocalDateTime.now());
        session.setLastActiveTime(LocalDateTime.now());
        return sessionRepository.save(session); // Saves the session to Redis.
    }

    public Session getSession(String sessionId) {
        return sessionRepository.findById(sessionId).orElse(null); // Retrieves a session by ID.
    }

    public Iterable<Session> getAllSessions() {
        return sessionRepository.findAll(); // Retrieves all sessions.
    }

    public void updateLastActiveTime(String sessionId) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setLastActiveTime(LocalDateTime.now());
            sessionRepository.save(session); // Updates the session's last active time.
        }
    }

    public void deleteSession(String sessionId) {
        sessionRepository.deleteById(sessionId); // Deletes a session by ID.
    }
}
```

The `SessionService` class is a Spring service that handles business logic related to user sessions, typically interacting with Redis to store and manage session data. It uses a `SessionRepository` to interact with Redis for CRUD operations. Below is a detailed explanation of the class and its methods:

### Detailed Breakdown:

1. **Constructor Injection:**
   ```java
   public SessionService(SessionRepository sessionRepository) {
       this.sessionRepository = sessionRepository;
   }
   ```
    - The `SessionService` constructor injects a `SessionRepository` instance. This repository provides the methods for interacting with the Redis database, allowing the service to save, retrieve, update, and delete sessions.

2. **`createSession(Long userId)` - Creates a new session:**
   ```java
   public Session createSession(Long userId) {
       Session session = new Session();
       session.setUserId(userId);
       session.setLoginTime(LocalDateTime.now());
       session.setLastActiveTime(LocalDateTime.now());
       return sessionRepository.save(session); // Saves the session to Redis.
   }
   ```
    - A new `Session` object is created with the given `userId`.
    - The `loginTime` and `lastActiveTime` are both set to the current timestamp (`LocalDateTime.now()`).
    - The session is saved to Redis via the `sessionRepository.save(session)` method, where it is stored as a Redis hash with the session data.

3. **`getSession(String sessionId)` - Retrieves a session by ID:**
   ```java
   public Session getSession(String sessionId) {
       return sessionRepository.findById(sessionId).orElse(null); // Retrieves a session by ID.
   }
   ```
    - This method fetches a session from Redis by its `sessionId` using `sessionRepository.findById(sessionId)`.
    - If the session is not found, it returns `null` (as indicated by `orElse(null)`).

4. **`getAllSessions()` - Retrieves all sessions:**
   ```java
   public Iterable<Session> getAllSessions() {
       return sessionRepository.findAll(); // Retrieves all sessions.
   }
   ```
    - This method fetches all session records stored in Redis using `sessionRepository.findAll()`.
    - It returns an iterable collection of `Session` objects.

5. **`updateLastActiveTime(String sessionId)` - Updates the session's last active time:**
   ```java
   public void updateLastActiveTime(String sessionId) {
       Session session = sessionRepository.findById(sessionId).orElse(null);
       if (session != null) {
           session.setLastActiveTime(LocalDateTime.now());
           sessionRepository.save(session); // Updates the session's last active time.
       }
   }
   ```
    - The method first checks if the session with the given `sessionId` exists in Redis.
    - If the session is found, its `lastActiveTime` is updated to the current timestamp (`LocalDateTime.now()`).
    - The updated session is saved back to Redis with `sessionRepository.save(session)`.

6. **`deleteSession(String sessionId)` - Deletes a session by ID:**
   ```java
   public void deleteSession(String sessionId) {
       sessionRepository.deleteById(sessionId); // Deletes a session by ID.
   }
   ```
    - This method deletes the session from Redis using `sessionRepository.deleteById(sessionId)`.

### Key Concepts:

- **`@Service`**: This annotation marks the class as a service component in Spring, allowing it to be registered as a bean in the Spring context. The service handles the business logic for user sessions.

- **`SessionRepository`**: This is a Spring Data repository interface that interacts with Redis. It provides methods such as `findById`, `findAll`, `save`, and `deleteById` for managing `Session` entities in Redis.

- **Session Lifecycle:**
    - **Creation**: When a user logs in, a new session is created and stored in Redis.
    - **Retrieval**: The session can be retrieved using its session ID.
    - **Update**: The session's `lastActiveTime` is updated when the user performs an activity.
    - **Deletion**: The session is deleted from Redis when it is no longer needed (e.g., when the user logs out or the session expires).



### Controllers for User and Session Management

The `SessionController` and `UserController` handle HTTP requests related to sessions and users. They define the endpoints for managing sessions and users, utilizing `SessionService` and `UserService`, respectively, to interact with Redis and PostgreSQL.

#### **SessionController**

```java
package com.mahmud.redispostgres.controller;

import com.mahmud.redispostgres.model.Session;
import com.mahmud.redispostgres.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions") // Defines the base URL for session-related endpoints.
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping // Creates a new session for a user.
    public ResponseEntity<Session> createSession(@RequestParam Long userId) {
        return ResponseEntity.ok(sessionService.createSession(userId));
    }

    @GetMapping // Retrieves all active sessions.
    public ResponseEntity<Iterable<Session>> getAllSessions() {
        Iterable<Session> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{sessionId}") // Retrieves a specific session by its ID.
    public ResponseEntity<Session> getSession(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if (session != null) {
            return ResponseEntity.ok(session); // Returns 200 OK with the session data.
        } else {
            return ResponseEntity.notFound().build(); // Returns 404 Not Found if session does not exist.
        }
    }

    @PutMapping("/{sessionId}/active") // Updates the last active time for a session.
    public ResponseEntity<Void> updateLastActiveTime(@PathVariable String sessionId) {
        sessionService.updateLastActiveTime(sessionId);
        return ResponseEntity.noContent().build(); // Returns 204 No Content after updating.
    }

    @DeleteMapping("/{sessionId}") // Deletes a session by its ID.
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        sessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build(); // Returns 204 No Content after deletion.
    }
}
```

**Key Endpoints in `SessionController`:**
1. **POST `/api/sessions`**: Creates a new session for a given user (`userId`), storing it in Redis.
2. **GET `/api/sessions`**: Retrieves all sessions stored in Redis.
3. **GET `/api/sessions/{sessionId}`**: Fetches a session by its `sessionId` from Redis.
4. **PUT `/api/sessions/{sessionId}/active`**: Updates the `lastActiveTime` of a session to reflect its most recent activity.
5. **DELETE `/api/sessions/{sessionId}`**: Deletes a session from Redis by its `sessionId`.

---

#### **UserController**

```java
package com.mahmud.redispostgres.controller;

import com.mahmud.redispostgres.model.User;
import com.mahmud.redispostgres.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users") // Defines the base URL for user-related endpoints.
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping // Retrieves all users, either from Redis cache or PostgreSQL.
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping // Creates a new user and saves it to PostgreSQL.
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @GetMapping("/{id}") // Retrieves a single user by ID.
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user); // Returns 200 OK with user data.
        } else {
            return ResponseEntity.notFound().build(); // Returns 404 if user not found.
        }
    }

    @PutMapping("/{id}") // Updates user data by ID.
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(user); // Returns 200 OK with updated user data.
    }

    @DeleteMapping("/{id}") // Deletes a user from PostgreSQL and removes the cache entry.
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // Returns 204 No Content after deletion.
    }
}
```

**Key Endpoints in `UserController`:**
1. **GET `/api/users`**: Retrieves a list of all users, either from Redis cache or PostgreSQL, depending on caching setup.
2. **POST `/api/users`**: Creates a new user and saves it to PostgreSQL (also updates the Redis cache).
3. **GET `/api/users/{id}`**: Retrieves a user by `id` from PostgreSQL, with caching applied based on the user's ID.
4. **PUT `/api/users/{id}`**: Updates an existing user's data and updates the Redis cache.
5. **DELETE `/api/users/{id}`**: Deletes a user from PostgreSQL and invalidates the corresponding cache.


### **Running the Application**
1. Start Redis and PostgreSQL using Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. Build and run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```

3. Access the Swagger UI at:
   ```
   http://localhost:8080/swagger-ui.html
   ```

---

### **Conclusion**
This article provided a comprehensive guide to building a hybrid application with **Redis** and **PostgreSQL** in Spring Boot. We covered everything from setting up the environment to implementing caching, session management, and REST APIs. By leveraging Redis for caching and PostgreSQL for persistent storage, you can build high-performance applications that are both fast and reliable.

Feel free to extend this project by adding more advanced Redis features like distributed locks, leaderboards, or real-time messaging!