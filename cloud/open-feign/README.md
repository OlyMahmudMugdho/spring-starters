# Building an Application with Spring Boot 3 and Spring Cloud OpenFeign

In this tutorial, we'll walk through the process of building an application using Spring Boot and OpenFeign. This application will interact with the JSONPlaceholder API, a free online REST API that you can use for testing and prototyping.
## Prerequisites

- Basic knowledge of Java and Spring Boot.
- Familiarity with RESTful APIs.
- A development environment set up with Java and Maven.

## Project Overview

We’ll create a Spring Boot application that performs CRUD operations on a `Post` resource using OpenFeign to interact with the JSONPlaceholder API. The application will include:

- A Feign client to define the external API interactions.
- A service layer to handle business logic.
- A REST controller to expose endpoints.

The JSONPlaceholder API (https://jsonplaceholder.typicode.com) provides a simple, mock REST API with endpoints for resources like posts, users, and comments. For this tutorial, we’ll focus on the `/posts` endpoint, which supports all CRUD operations.

## Why Use OpenFeign?

**OpenFeign** is a declarative REST client that reduces the complexity of making HTTP requests. Instead of writing low-level HTTP client code (e.g., with `RestTemplate` or `HttpClient`), you define an interface with annotations, and OpenFeign handles the rest.

## Step-by-Step Guide

### Step 1: Set Up Your Spring Boot Project

1. **Create a New Spring Boot Project**:
    - Use [Spring Initializr](https://start.spring.io/) to generate a new Spring Boot project with the following settings:
        - **Project**: Maven
        - **Language**: Java
        - **Spring Boot Version**: 3.4.3
        - **Java Version**: 21
        - **Dependencies**:
            - Spring Web
            - Spring Cloud OpenFeign
            - Springdoc OpenAPI (for API documentation)
    - Download the zip file, extract it, and open it in your IDE.

2. **Add Dependencies**:
    - Ensure your `pom.xml` includes the necessary dependencies:

   ```xml
   <dependencies>
       <!-- Spring Boot Starter Web -->
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-web</artifactId>
       </dependency>

       <!-- Spring Cloud Starter OpenFeign -->
       <dependency>
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-starter-openfeign</artifactId>
       </dependency>

       <!-- Springdoc OpenAPI for API Documentation -->
       <dependency>
           <groupId>org.springdoc</groupId>
           <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
           <version>2.8.5</version>
       </dependency>
   </dependencies>
   ```

### Step 2: Configure Your Application

1. **Application Properties**:
    - Create an `application.properties` file in the `src/main/resources` directory with the following content:

   ```properties
   spring.application.name=open-feign
   spring.cloud.openfeign.client.config.default.connectTimeout=5000
   spring.cloud.openfeign.client.config.default.readTimeout=5000
   spring.cloud.openfeign.client.config.default.logger-level=full
   ```

2. **Enable Feign Clients**:
    - Add the `@EnableFeignClients` annotation to your main application class (e.g., `OpenFeignApplication.java`):

   ```java
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cloud.openfeign.EnableFeignClients;

   @SpringBootApplication
   @EnableFeignClients
   public class OpenFeignApplication {
       public static void main(String[] args) {
           SpringApplication.run(OpenFeignApplication.class, args);
       }
   }
   ```

### Step 3: Define the Data Transfer Object (DTO)

**Create the `Post` Class**:
    - In the `com.mahmud.openfeign.dto` package, create a `Post` class to represent the data structure:

   ```java
   package com.mahmud.openfeign.dto;

   import jakarta.validation.constraints.NotNull;
   import jakarta.validation.constraints.Size;

   /**
    * Data Transfer Object (DTO) representing a Post entity.
    * This class is used to transfer data between the client and the server.
    */
   public class Post {
       private Integer userId;
       private Integer id;

       @NotNull
       @Size(min = 1, max = 255)
       private String title;

       @NotNull
       @Size(min = 1)
       private String body;

       // Getters and setters
       public Integer getUserId() {
           return userId;
       }

       public void setUserId(Integer userId) {
           this.userId = userId;
       }

       public Integer getId() {
           return id;
       }

       public void setId(Integer id) {
           this.id = id;
       }

       public String getTitle() {
           return title;
       }

       public void setTitle(String title) {
           this.title = title;
       }

       public String getBody() {
           return body;
       }

       public void setBody(String body) {
           this.body = body;
       }
   }
   ```

   **Breakdown:**
    - **Purpose**: Represents a post with fields for `userId`, `id`, `title`, and `body`.
    - **Annotations**: `@NotNull` and `@Size` enforce validation constraints on the `title` and `body` fields.
    - **Usage**: This DTO is used to transfer post data between the client and server, ensuring that the data adheres to specified validation rules.

### Step 4: Create the Feign Client

**Define the `PostClient` Interface**:
    - In the `com.mahmud.openfeign.client` package, create a `PostClient` interface to define the CRUD operations:

   ```java
   package com.mahmud.openfeign.client;

   import com.mahmud.openfeign.dto.Post;
   import org.springframework.cloud.openfeign.FeignClient;
   import org.springframework.web.bind.annotation.*;

   import java.util.List;

   /**
    * Feign client interface to interact with the JSONPlaceholder API.
    * This interface defines the methods for performing CRUD operations on the Post resource.
    */
   @FeignClient(name = "postClient", url = "https://jsonplaceholder.typicode.com")
   public interface PostClient {

       // Retrieves a list of all posts.
       // @return A list of Post objects.
       @GetMapping("/posts")
       List<Post> getPosts();

       // Retrieves a post by its ID.
       // @param id The ID of the post to retrieve.
       // @return The Post object with the specified ID.
       @GetMapping("/posts/{id}")
       Post getPostById(@PathVariable("id") Integer id);

       // Creates a new post.
       // @param post The Post object to create.
       // @return The created Post object.
       @PostMapping("/posts")
       Post createPost(@RequestBody Post post);

       // Updates an existing post.
       // @param id   The ID of the post to update.
       // @param post The Post object with updated data.
       // @return The updated Post object.
       @PutMapping("/posts/{id}")
       Post updatePost(@PathVariable("id") Integer id, @RequestBody Post post);

       // Deletes a post by its ID.
       // @param id The ID of the post to delete.
       @DeleteMapping("/posts/{id}")
       void deletePost(@PathVariable("id") Integer id);
   }
   ```

   **Breakdown:**
    - **Purpose**: Defines the CRUD operations for interacting with the JSONPlaceholder API.
    - **Annotations**: `@FeignClient` specifies the name and URL of the Feign client. `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` define the HTTP methods and endpoints for each operation.
    - **Usage**: This interface is used by the service layer to perform HTTP requests to the external API.

### Step 5: Implement the Service Layer

**Create the `PostService` Class**:
    - In the `com.mahmud.openfeign.service` package, create a `PostService` class to handle the business logic:

   ```java
   package com.mahmud.openfeign.service;

   import com.mahmud.openfeign.client.PostClient;
   import com.mahmud.openfeign.dto.Post;
   import org.springframework.stereotype.Service;

   import java.util.List;

   /**
    * Service class that handles the business logic for Post operations.
    * This class uses the PostClient Feign client to interact with the external API.
    */
   @Service
   public class PostService {
       private final PostClient postClient;

       // Constructor for PostService
       public PostService(PostClient postClient) {
           this.postClient = postClient;
       }

       // Retrieves all posts
       public List<Post> getAllPosts() {
           return postClient.getPosts();
       }

       // Retrieves a post by its ID
       public Post getPostById(Integer id) {
           return postClient.getPostById(id);
       }

       // Creates a new post
       public Post createPost(Post post) {
           return postClient.createPost(post);
       }

       // Updates an existing post
       public Post updatePost(Integer id, Post post) {
           return postClient.updatePost(id, post);
       }

       // Deletes a post by its ID
       public void deletePost(Integer id) {
           postClient.deletePost(id);
       }
   }
   ```

   **Breakdown:**
    - **Purpose**: Contains the business logic for performing CRUD operations on the `Post` resource.
    - **Methods**: Each method corresponds to a CRUD operation and uses the `PostClient` Feign client to interact with the external API.
    - **Usage**: This service is used by the controller to handle HTTP requests and perform the necessary operations.

### Step 6: Create the REST Controller

**Define the `PostController` Class**:
    - In the `com.mahmud.openfeign.controller` package, create a `PostController` class to expose the CRUD endpoints:

   ```java
   package com.mahmud.openfeign.controller;

   import com.mahmud.openfeign.dto.Post;
   import com.mahmud.openfeign.service.PostService;
   import org.springframework.web.bind.annotation.*;

   import java.util.List;

   /**
    * REST controller that exposes endpoints for CRUD operations on the Post resource.
    * This controller uses the PostService to handle the business logic.
    */
   @RestController
   @RequestMapping("/api/v1/posts")
   public class PostController {
       private final PostService postService;
        
       // PostService added using constructor injection
       public PostController(PostService postService) {
           this.postService = postService;
       }

       @GetMapping
       public List<Post> getAllPosts() {
           return postService.getAllPosts();
       }

       // Retrieves a post by its ID
       @GetMapping("/{id}")
       public Post getPostById(@PathVariable("id") Integer id) {
           return postService.getPostById(id);
       }

       // Creates a new post
       @PostMapping
       public Post createPost(@RequestBody Post post) {
           return postService.createPost(post);
       }

       // Updates an existing post
       @PutMapping("/{id}")
       public Post updatePost(@PathVariable("id") Integer id, @RequestBody Post post) {
           return postService.updatePost(id, post);
       }

       // Deletes a post by its ID
       @DeleteMapping("/{id}")
       public void deletePost(@PathVariable("id") Integer id) {
           postService.deletePost(id);
       }
   }
   ```

   **Breakdown:**
    - **Purpose**: Exposes RESTful endpoints for performing CRUD operations on the `Post` resource.
    - **Annotations**: `@RestController` indicates that the class is a REST controller. `@RequestMapping("/api/v1/posts")` specifies the base URL for the controller's endpoints.
    - **Endpoints**: `GET /api/v1/posts` retrieves all posts. `GET /api/v1/posts/{id}` retrieves a post by ID. `POST /api/v1/posts` creates a new post. `PUT /api/v1/posts/{id}` updates an existing post. `DELETE /api/v1/posts/{id}` deletes a post by ID.
    - **Usage**: This controller handles HTTP requests and uses the `PostService` to perform the necessary business logic.

### Step 7: Run and Test Your Application

1. **Run the Application**:
    - Start your Spring Boot application by running the main class.

2. **Test the Endpoints**:
    - Use a tool like Postman or cURL to test the CRUD endpoints:
        - `GET /api/v1/posts`: Retrieve all posts.
        - `GET /api/v1/posts/{id}`: Retrieve a post by ID.
        - `POST /api/v1/posts`: Create a new post.
        - `PUT /api/v1/posts/{id}`: Update an existing post.
        - `DELETE /api/v1/posts/{id}`: Delete a post by ID.

3. **Access API Documentation**:
    - Navigate to `http://localhost:8080/swagger-ui.html` to view the auto-generated API documentation provided by Springdoc OpenAPI.

## Conclusion

In this tutorial, you learned how to build a CRUD application using Spring Boot 3.4.3 and OpenFeign with Java 21 to interact with the JSONPlaceholder API. This setup provides a clean and efficient way to perform HTTP operations and can be easily adapted to work with other RESTful APIs. By following this guide, you can create robust and maintainable applications that adhere to industry standards.

