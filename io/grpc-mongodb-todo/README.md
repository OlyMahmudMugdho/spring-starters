# Implementing gRPC with Spring Boot 3 and Spring gRPC: A Comprehensive Guide

In modern microservices, efficient and reliable inter-service communication is paramount. gRPC, an open-source Remote Procedure Call (RPC) framework developed by Google, leverages HTTP/2 and Protocol Buffers (protobuf) to provide high-performance, strongly-typed service interactions. This guide offers a detailed, step-by-step walkthrough of building a Todo application using Spring Boot 3, Spring gRPC (currently experimental), Kotlin, and MongoDB. We’ll implement the gRPC server first, followed by the client, ensuring a clear, sequential development process.

## What is gRPC?

gRPC is a high-performance RPC framework that uses HTTP/2 for transport and Protocol Buffers for defining services and messages. It supports unary calls (single request-response) and streaming (client, server, or bidirectional), making it ideal for microservices requiring low latency and high throughput. Unlike REST, which typically uses JSON over HTTP/1.1, gRPC’s binary serialization and contract-first design enhance efficiency and reliability.

### Benefits of gRPC Over REST

1. **Performance**: HTTP/2’s multiplexing and header compression, paired with Protobuf’s compact binary format, outperform JSON-based REST APIs.
2. **Strong Typing**: Protobuf enforces a schema, reducing runtime errors compared to REST’s loosely typed payloads.
3. **Streaming**: gRPC supports real-time communication via streaming, unlike REST’s static request-response model.
4. **Code Generation**: Protobuf generates client and server code across multiple languages, streamlining development.
5. **Interoperability**: Its language-agnostic nature suits polyglot environments.

While REST is simpler and browser-friendly, gRPC excels in internal service communication for performance-critical systems.

## Key Terms in gRPC

- **Protocol Buffers (protobuf)**: A serialization format and Interface Definition Language (IDL) for defining services and messages.
- **Service**: A set of RPC methods (e.g., `CreateTodo`) defined in a `.proto` file.
- **Stub**: Auto-generated code for clients to call server methods (e.g., `TodoServiceGrpc.TodoServiceBlockingStub`).
- **Channel**: An abstraction over the HTTP/2 connection between client and server.
- **Unary RPC**: A single request-response interaction, used in this guide.
- **Streaming RPC**: Continuous data exchange, not covered here but supported by gRPC.

## gRPC Server Implementation

Let’s build the `grpc-server` to manage Todo operations and persist data in MongoDB.

### Step 1: Project Setup

1. **Create the Project**:
   - Use Spring Initializr ([start.spring.io](https://start.spring.io/)) to generate a Spring Boot 3 project with Kotlin.
   - I have set the group to `com.mahmud`, artifact to `grpc-server`, and selected Kotlin as language, project type Gradle (Kotlin).

2. **Add Dependencies**:
   - include:
      - `spring-boot-starter-data-mongodb-reactive`
      - `spring-grpc-spring-boot-starter`


3. **Configure MongoDB**:
   - Ensure MongoDB is running locally `mongodb://localhost:27017`.
   - Create `application.yaml`:
     ```yaml
     spring:
       application:
         name: grpc-server
       data:
         mongodb:
           uri: mongodb://localhost:27017/todo_db
     ```

### Step 2: Define the Protobuf File

Create `todo.proto` to define the service and messages:

```protobuf
syntax = "proto3";

package todo;

option java_multiple_files = true;
option java_package = "com.mahmud.grpcserver";
option java_outer_classname = "TodoProto";

// The Todo service definition
service TodoService {
  // Create a new todo item
  rpc CreateTodo (TodoRequest) returns (TodoResponse) {}
  // Get a todo item by ID
  rpc GetTodo (TodoIdRequest) returns (TodoResponse) {}
  // List all todo items
  rpc ListTodos (ListTodosRequest) returns (ListTodosResponse) {}
  // Update a todo item
  rpc UpdateTodo (TodoRequest) returns (TodoResponse) {}
  // Delete a todo item
  rpc DeleteTodo (TodoIdRequest) returns (Empty) {}
}

// Message definitions
message TodoItem {
  string id = 1;
  string title = 2;
  string description = 3;
  bool completed = 4;
  google.protobuf.Timestamp created_at = 5;
  google.protobuf.Timestamp updated_at = 6;
  optional google.protobuf.Timestamp due_date = 7;
}

message TodoRequest {
  string id = 1;          // Empty for create, required for update
  string title = 2;
  string description = 3;
  bool completed = 4;
  optional google.protobuf.Timestamp due_date = 5;
}

message TodoResponse {
  TodoItem todo = 1;
}

message TodoIdRequest {
  string id = 1;
}

message ListTodosRequest {
  optional bool show_completed = 1;    // Filter for completed todos
  int32 page_size = 2;                 // Number of items per page
  string page_token = 3;              // Pagination token
}

message ListTodosResponse {
  repeated TodoItem todos = 1;
  string next_page_token = 2;
}

message Empty {}

// Import Google timestamp for timestamp fields
import "google/protobuf/timestamp.proto";
```
The proto defines the `TodoService` with five unary RPC methods for CRUD operations. Messages like `TodoItem` and `TodoRequest` use Protobuf scalars and `Timestamp` for date-time fields.

**Breakdown**:
- `service TodoService`: Specifies CRUD methods.
- `TodoItem`: Represents a Todo with fields like `id` and optional `due_date`.
- `repeated`: Indicates a list (e.g., `todos` in `ListTodosResponse`).
- `optional`: Marks nullable fields.

### Step 3: Generate gRPC Stubs

Run:

```bash
./gradlew build
```

This compiles the Protobuf file and generates stubs in `build/generated/source/proto/main`, including `TodoServiceGrpc.java`. In IntelliJ IDEA, right-click the generated folder and select "Mark Directory As → Generated Source Root".

### Step 4: Implement Server Logic

Create the domain model. It defines the `Todo` entity for MongoDB persistence.
#### `Todo.kt`

```kotlin
package com.mahmud.grpcserver.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "todos")
data class Todo(
    @Id
    val id: String? = null,
    val title: String,
    val description: String,
    val completed: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val dueDate: Instant? = null
)
```

**Breakdown**:
- `@Document(collection = "todos")`: Maps to the `todos` collection.
- `@Id`: Unique identifier, nullable for new entries.
- `Instant`: Matches Protobuf’s `Timestamp`.

Create the repository interface. It provides reactive CRUD operations with a custom filter method.

#### `TodoRepository.kt`

```kotlin
package com.mahmud.grpcserver.repository

import com.mahmud.grpcserver.model.Todo
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface TodoRepository : ReactiveMongoRepository<Todo, String> {
    fun findByCompleted(completed: Boolean): Flux<Todo>
}
```

**Breakdown**:
- `ReactiveMongoRepository<Todo, String>`: Manages Todo entities.
- `Flux<Todo>`: Returns a reactive stream.

Implement the gRPC service. It will handle our main logics.
#### `TodoService.kt`

```kotlin
package com.mahmud.grpcserver.service

import com.mahmud.grpcserver.model.Todo
import com.google.protobuf.Timestamp
import com.mahmud.grpcserver.*
import com.mahmud.grpcserver.repository.TodoRepository
import io.grpc.stub.StreamObserver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.grpc.server.service.GrpcService
import reactor.core.publisher.Mono
import java.time.Instant

@GrpcService
class TodoService @Autowired constructor(
   private val todoRepository: TodoRepository
) : TodoServiceGrpc.TodoServiceImplBase() {

   // gRPC method to create a new Todo item
   override fun createTodo(request: TodoRequest, responseObserver: StreamObserver<TodoResponse>) {
      val todo = Todo(
         title = request.title,
         description = request.description,
         completed = request.completed,
         dueDate = request.dueDate?.toInstant() // Convert protobuf Timestamp to Java Instant
      )
      // Save Todo to database using reactive programming
      todoRepository.save(todo)
         .map { TodoResponse.newBuilder().setTodo(it.toProto()).build() }
         .subscribe(
            { response -> responseObserver.onNext(response); responseObserver.onCompleted() },
            { error -> responseObserver.onError(error) }
         )
   }

   // gRPC method to retrieve a Todo by its ID
   override fun getTodo(request: TodoIdRequest, responseObserver: StreamObserver<TodoResponse>) {
      todoRepository.findById(request.id)
         .switchIfEmpty(Mono.error(RuntimeException("Todo not found"))) // Handle missing Todo
         .map { TodoResponse.newBuilder().setTodo(it.toProto()).build() }
         .subscribe(
            { response -> responseObserver.onNext(response); responseObserver.onCompleted() },
            { error -> responseObserver.onError(error) }
         )
   }

   // gRPC method to list all Todos with an optional filter for completed ones
   override fun listTodos(request: ListTodosRequest, responseObserver: StreamObserver<ListTodosResponse>) {
      val todos = if (request.showCompleted) {
         todoRepository.findAll() // Fetch all Todos
      } else {
         todoRepository.findByCompleted(false) // Fetch only incomplete Todos
      }
      todos.collectList()
         .map { items ->
            ListTodosResponse.newBuilder()
               .addAllTodos(items.map { it.toProto() }) // Convert to protobuf format
               .build()
         }
         .subscribe(
            { response -> responseObserver.onNext(response); responseObserver.onCompleted() },
            { error -> responseObserver.onError(error) }
         )
   }

   // gRPC method to update an existing Todo item
   override fun updateTodo(request: TodoRequest, responseObserver: StreamObserver<TodoResponse>) {
      todoRepository.findById(request.id)
         .switchIfEmpty(Mono.error(RuntimeException("Todo not found"))) // Handle missing Todo
         .map { existing ->
            existing.copy(
               title = request.title,
               description = request.description,
               completed = request.completed,
               dueDate = request.dueDate?.toInstant(),
               updatedAt = Instant.now() // Update timestamp
            )
         }
         .flatMap { todoRepository.save(it) } // Save updated Todo
         .map { TodoResponse.newBuilder().setTodo(it.toProto()).build() }
         .subscribe(
            { response -> responseObserver.onNext(response); responseObserver.onCompleted() },
            { error -> responseObserver.onError(error) }
         )
   }

   // gRPC method to delete a Todo item by its ID
   override fun deleteTodo(request: TodoIdRequest?, responseObserver: StreamObserver<Empty>?) {
      request?.let {
         todoRepository.deleteById(it.id)
            .then(Mono.just(Empty.newBuilder().build())) // Return an empty response
            .subscribe(
               { response -> responseObserver?.onNext(response); responseObserver?.onCompleted() },
               { error -> responseObserver?.onError(error) }
            )
      } ?: run {
         // If request is null, complete the response
         responseObserver?.onNext(Empty.newBuilder().build())
         responseObserver?.onCompleted()
      }
   }
}

// Extension function to convert a Todo entity to a gRPC TodoItem message
fun Todo.toProto(): TodoItem {
   val builder = TodoItem.newBuilder()
      .setId(id!!)
      .setTitle(title)
      .setDescription(description)
      .setCompleted(completed)
      .setCreatedAt(Timestamp.newBuilder()
         .setSeconds(createdAt.epochSecond)
         .setNanos(createdAt.nano)
         .build())
      .setUpdatedAt(Timestamp.newBuilder()
         .setSeconds(updatedAt.epochSecond)
         .setNanos(updatedAt.nano)
         .build())
   dueDate?.let {
      builder.setDueDate(Timestamp.newBuilder()
         .setSeconds(it.epochSecond)
         .setNanos(it.nano)
         .build())
   }
   return builder.build()
}

// Extension function to convert a gRPC Timestamp to Java Instant
fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(seconds, nanos.toLong())
```

**Breakdown**:
- `@GrpcService`: Integrates with Spring gRPC.
- Comments explain gRPC request handling, response building, and call completion.

### Step 5: Run the Server

Ensure MongoDB is running.

Run:

```bash
./gradlew bootRun
```

The gRPC server listens on `localhost:9090`.

### Let's test the gGRPC Server using grpcurl

`grpcurl` is a command-line tool for interacting with gRPC servers. These commands assume the server supports reflection (common in development) or that you have the `todo.proto` file locally. If reflection isn’t enabled, add `-proto todo.proto` to each command and ensure the file is in your working directory.

#### 1. Create Todo
Creates a new Todo item.

```bash
grpcurl -plaintext -d '{
  "title": "Buy groceries",
  "description": "Milk, bread, eggs",
  "completed": false,
  "due_date": {"seconds": 1735689600, "nanos": 0}
}' localhost:9090 todo.TodoService/CreateTodo
```
- `-plaintext`: No TLS (matches server config).
- `-d`: JSON payload for the `TodoRequest` message.
- `due_date`: Set to a future timestamp (e.g., Dec 31, 2025, 00:00:00 UTC).
- Expected response: `TodoResponse` with the created `TodoItem`.

#### 2. Get Todo
Retrieves a Todo by ID (replace `<todo-id>` with an actual ID from a prior `CreateTodo` response).

```bash
grpcurl -plaintext -d '{"id": "<todo-id>"}' localhost:9090 todo.TodoService/GetTodo
```
- Payload: `TodoIdRequest` with the Todo’s ID.
- Expected response: `TodoResponse` with the matching `TodoItem`.

#### 3. List Todos
Lists all Todo items, optionally filtering by completion status.

```bash
grpcurl -plaintext -d '{"show_completed": true, "page_size": 10}' localhost:9090 todo.TodoService/ListTodos
```
- `show_completed`: `true` to include completed Todos; set to `false` to filter them out.
- `page_size`: Limits results (e.g., 10 items).
- Expected response: `ListTodosResponse` with a list of `TodoItem`s.

#### 4. Update Todo
Updates an existing Todo (replace `<todo-id>` with an actual ID).

```bash
grpcurl -plaintext -d '{
  "id": "<todo-id>",
  "title": "Buy groceries updated",
  "description": "Milk, bread, eggs, cheese",
  "completed": true,
  "due_date": {"seconds": 1735776000, "nanos": 0}
}' localhost:9090 todo.TodoService/UpdateTodo
```
- Payload: `TodoRequest` with updated fields.
- `due_date`: Updated to Jan 1, 2026, 00:00:00 UTC.
- Expected response: `TodoResponse` with the updated `TodoItem`.

#### 5. Delete Todo
Deletes a Todo by ID (replace `<todo-id>` with an actual ID).

```bash
grpcurl -plaintext -d '{"id": "<todo-id>"}' localhost:9090 todo.TodoService/DeleteTodo
```
- Payload: `TodoIdRequest` with the Todo’s ID.
- Expected response: Empty message (`{}`).



## gRPC Client Implementation

Now, let’s build the `grpc-client` to expose REST endpoints and interact with the server.

### Step 1: Project Setup

1. **Create the Project**:
   - Use Spring Initializr to generate a Spring Boot 3 project with Kotlin.
   - In my case, I have set the group to `com.mahmud`, artifact to `grpc-client`.

2. **Add Dependencies**:
   - Include the following dependencies:
     ```kotlin
       org.springframework.boot:spring-boot-starter-webflux
       org.springframework.grpc:spring-grpc-spring-boot-starter
     ```

3. **Configure Application**:
   - Create `application.yaml`:
     ```yaml
     spring:
       application:
         name: grpc-client
       grpc:
         server:
           port: 9091
     ```

### Step 2: Define the Protobuf File

Create `todo.proto` file in `src/main/proto` directory. It defines the client’s `TodoService`, matching the server’s contract with a different package. Identical to the server’s `todo.proto` except for `java_package`.


```protobuf
syntax = "proto3";

package todo;

option java_multiple_files = true;
option java_package = "com.mahmud.grpcclient";
option java_outer_classname = "TodoProto";

service TodoService {
   rpc CreateTodo (TodoRequest) returns (TodoResponse) {}
   rpc GetTodo (TodoIdRequest) returns (TodoResponse) {}
   rpc ListTodos (ListTodosRequest) returns (ListTodosResponse) {}
   rpc UpdateTodo (TodoRequest) returns (TodoResponse) {}
   rpc DeleteTodo (TodoIdRequest) returns (Empty) {}
}

message TodoItem {
   string id = 1;
   string title = 2;
   string description = 3;
   bool completed = 4;
   google.protobuf.Timestamp created_at = 5;
   google.protobuf.Timestamp updated_at = 6;
   optional google.protobuf.Timestamp due_date = 7;
}

message TodoRequest {
   string id = 1;
   string title = 2;
   string description = 3;
   bool completed = 4;
   optional google.protobuf.Timestamp due_date = 5;
}

message TodoResponse {
   TodoItem todo = 1;
}

message TodoIdRequest {
   string id = 1;
}

message ListTodosRequest {
   optional bool show_completed = 1;
   int32 page_size = 2;
   string page_token = 3;
}

message ListTodosResponse {
   repeated TodoItem todos = 1;
   string next_page_token = 2;
}

message Empty {}

import "google/protobuf/timestamp.proto";
```

### Step 3: Generate gRPC Stubs

Run:

```bash
./gradlew build
```

This generates stubs in `build/generated/source/proto/main`. Mark as a generated source root in your IDE.

### Step 4: Implement Client Logic

Let's configure the gRPC client according to the grpc-server.

#### `GrpcClientConfig.kt`

```kotlin
package com.mahmud.grpcclient.config
import com.mahmud.grpcclient.TodoServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcClientConfig {

   @Bean
   fun todoServiceStub(): TodoServiceGrpc.TodoServiceBlockingStub {
      val channel: ManagedChannel = ManagedChannelBuilder
         .forAddress("localhost", 9090) // Connects to our gRPC server
         .usePlaintext() // No TLS for simplicity
         .build()
      return TodoServiceGrpc.newBlockingStub(channel)
   }
}
```
**Breakdown**:
- We just set up a stub for server communication.


### Implement the gRPC calls
The `TodoClientService` handles gRPC calls by communicating with the server.
#### `TodoClientService.kt`

```kotlin
package com.mahmud.grpcclient.service

import com.google.protobuf.Timestamp
import com.mahmud.grpcclient.*
import org.springframework.stereotype.Service
import java.time.Instant

// Spring service managing gRPC client calls to the server
@Service
class TodoClientService(private val todoStub: TodoServiceGrpc.TodoServiceBlockingStub) {

   // gRPC call to create a new Todo on the server
   fun createTodo(title: String, description: String, completed: Boolean, dueDate: Instant?): TodoItem? {
      // Build gRPC request from method parameters
      val request = TodoRequest.newBuilder()
         .setTitle(title)
         .setDescription(description)
         .setCompleted(completed)
         .apply {
            dueDate?.let {
               setDueDate(Timestamp.newBuilder()
                  .setSeconds(it.epochSecond)
                  .setNanos(it.nano)
                  .build())
            }
         }
         .build()
      // Invoke gRPC server method and return response
      return todoStub.createTodo(request).todo;
   }

   // gRPC call to retrieve a Todo by its ID from the server
   fun getTodo(id: String): TodoItem {
      // Construct gRPC request with Todo ID
      val request = TodoIdRequest.newBuilder().setId(id).build()
      // Fetch Todo from gRPC server and return it
      return todoStub.getTodo(request).todo
   }

   // gRPC call to list Todos, optionally showing completed ones
   fun listTodos(showCompleted: Boolean): MutableList<TodoItem>? {
      // Create gRPC request with filter parameter
      val request = ListTodosRequest.newBuilder()
         .setShowCompleted(showCompleted)
         .build()
      // Retrieve list from gRPC server and return as MutableList
      return todoStub.listTodos(request).todosList
   }

   // gRPC call to update an existing Todo on the server
   fun updateTodo(id: String, title: String, description: String, completed: Boolean, dueDate: Instant?): TodoItem? {
      // Build gRPC request with updated Todo data
      val request = TodoRequest.newBuilder()
         .setId(id)
         .setTitle(title)
         .setDescription(description)
         .setCompleted(completed)
         .apply {
            dueDate?.let {
               setDueDate(Timestamp.newBuilder()
                  .setSeconds(it.epochSecond)
                  .setNanos(it.nano)
                  .build())
            }
         }
         .build()
      // Update Todo via gRPC server and return result
      return todoStub.updateTodo(request).todo
   }

   // gRPC call to delete a Todo by ID on the server
   fun deleteTodo(id: String): Empty? {
      // Construct gRPC request with ID to delete
      val request = TodoIdRequest.newBuilder().setId(id).build()
      // Delete Todo on gRPC server and return empty response
      return todoStub.deleteTodo(request)
   }
}
```

**Breakdown**:
- `@Service`: Marks as a Spring service.
- `todoStub`: Injected stub for calls.


### DTO
Let's define the REST response DTO for JSON serialization.

#### `TodoItemDto.kt`
```kotlin
package com.mahmud.grpcclient.dto

import com.mahmud.grpcclient.TodoItem

data class TodoItemDto(
   val id: String,
   val title: String,
   val description: String,
   val completed: Boolean,
   val createdAt: Long, // Epoch seconds
   val updatedAt: Long, // Epoch seconds
   val dueDate: Long? // dueDate nullable
)

fun mapToTodoItemDto(todoItem: TodoItem): TodoItemDto {
   return TodoItemDto(
      id = todoItem.id,
      title = todoItem.title,
      description = todoItem.description,
      completed = todoItem.completed,
      createdAt = todoItem.createdAt.seconds,
      updatedAt = todoItem.updatedAt.seconds,
      dueDate = todoItem.dueDate?.seconds // handle the null case
   )
}
```
Define the DTO for Todo Request:

#### `TodoRequestDto.kt`

```kotlin
package com.mahmud.grpcclient.dto

data class TodoRequestDto(
   val title: String,
   val description: String,
   val completed: Boolean = false,
   val dueDate: Long? = null // Epoch seconds 
)
```

### Expose REST endpoints
#### `TodoController.kt`

REST controller bridging HTTP requests to gRPC calls with detailed comments.

```kotlin
package com.mahmud.grpcclient.controller

import com.mahmud.grpcclient.Empty
import com.mahmud.grpcclient.service.TodoClientService
import com.mahmud.grpcclient.TodoItem
import com.mahmud.grpcclient.dto.TodoItemDto
import com.mahmud.grpcclient.dto.TodoRequestDto
import com.mahmud.grpcclient.dto.mapToTodoItemDto
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

// REST controller exposing Todo endpoints over HTTP
@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoClientService) {

   // POST endpoint to create a new Todo via gRPC
   @PostMapping
   fun createTodo(
      @RequestBody request: TodoRequestDto
   ): Mono<TodoItemDto> {
      // Delegate to gRPC service to create Todo
      val todo = todoService.createTodo(
         request.title,
         request.description,
         request.completed,
         request.dueDate?.let { Instant.ofEpochSecond(it) }
      )
      // Wrap gRPC response in reactive Mono for REST
      return Mono.justOrEmpty(todo?.let { mapToTodoItemDto(it) })
   }

   // GET endpoint to retrieve a Todo by ID via gRPC
   @GetMapping("/{id}")
   fun getTodo(@PathVariable id: String): Mono<TodoItemDto> {
      // Fetch Todo from gRPC service and map to DTO
      return Mono.justOrEmpty(mapToTodoItemDto(todoService.getTodo(id)))
   }

   // GET endpoint to list Todos, with optional completion filter
   @GetMapping
   fun listTodos(@RequestParam(defaultValue = "true") showCompleted: Boolean): Flux<TodoItemDto> {
      // Retrieve list from gRPC service
      val todoList = todoService.listTodos(showCompleted)
      // Convert gRPC list to reactive Flux of DTOs
      return todoList?.let { Flux.fromIterable(it).map { todoItem -> mapToTodoItemDto(todoItem) } } ?: Flux.empty()
   }

   // PUT endpoint to update a Todo via gRPC
   @PutMapping("/{id}")
   fun updateTodo(
      @PathVariable id: String,
      @RequestBody request: TodoRequestDto
   ): Mono<TodoItemDto> {
      // Update Todo using gRPC service
      val todo = todoService.updateTodo(
         id,
         request.title,
         request.description,
         request.completed,
         request.dueDate?.let { Instant.ofEpochSecond(it) }
      )
      // Return updated Todo as reactive Mono
      return Mono.justOrEmpty(todo?.let { mapToTodoItemDto(it) })
   }

   // DELETE endpoint to remove a Todo via gRPC
   @DeleteMapping("/{id}")
   fun deleteTodo(@PathVariable id: String): Empty? {
      // Call gRPC service to delete Todo and return empty response
      return todoService.deleteTodo(id)
   }
}
```

**Breakdown**:
- `@RestController`: Exposes REST endpoints.
- `Mono`/`Flux`: Handles reactive output.

### Step 5: Run the Client

```bash
./gradlew bootRun
```

### LEt's test the gRPC Client using curl

The client exposes REST endpoints at `http://localhost:8080/api/todos`, mapping to the gRPC server’s functionality.

#### 1. Create Todo (POST)
Creates a new Todo via the REST API.

```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Buy groceries",
    "description": "Milk, bread, eggs",
    "completed": false,
    "dueDate": 1735689600
  }'
```
- Payload: `TodoRequestDto` with `dueDate` as epoch seconds (Dec 31, 2025, 00:00:00 UTC).
- Response: JSON `TodoItemDto` with the created Todo.

#### 2. Get Todo (GET)
Retrieves a Todo by ID (replace `<todo-id>` with an actual ID).

```bash
curl -X GET http://localhost:8080/api/todos/<todo-id>
```
- Path: Uses the Todo’s ID.
- Response: JSON `TodoItemDto` with the Todo’s details.

#### 3. List Todos (GET)
Lists all Todos, with an optional `showCompleted` filter.

```bash
curl -X GET "http://localhost:8080/api/todos?showCompleted=true"
```
- Query param: `showCompleted=true` includes completed Todos; set to `false` to exclude them.
- Response: JSON array of `TodoItemDto`s.

#### 4. Update Todo (PUT)
Updates an existing Todo (replace `<todo-id>` with an actual ID).

```bash
curl -X PUT http://localhost:8080/api/todos/<todo-id> \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Buy groceries updated",
    "description": "Milk, bread, eggs, cheese",
    "completed": true,
    "dueDate": 1735776000
  }'
```
- Payload: `TodoRequestDto` with updated fields, `dueDate` as epoch seconds (Jan 1, 2026, 00:00:00 UTC).
- Response: JSON `TodoItemDto` with the updated Todo.

#### 5. Delete Todo (DELETE)
Deletes a Todo by ID (replace `<todo-id>` with an actual ID).

```bash
curl -X DELETE http://localhost:8080/api/todos/<todo-id>
```
- Path: Uses the Todo’s ID.
- Response: Empty response (HTTP 200 OK).


## Conclusion

This guide provides a complete implementation of a Todo application using gRPC with Spring Boot 3 and Spring gRPC. By building the server and client sequentially, we’ve demonstrated gRPC’s capabilities within a Spring ecosystem, leveraging Kotlin and MongoDB.

Key takeaways:
- gRPC’s performance and typing advantages make it ideal for service-to-service communication.
- Spring gRPC simplifies gRPC adoption in Spring Boot, despite its experimental status.
- Reactive programming enhances scalability.

For production, consider TLS, full reactive flows, and streaming features. As Spring gRPC matures, it will solidify its place in modern architectures.

## References

- [gRPC Official Documentation](https://grpc.io/docs/)
- [Spring gRPC GitHub](https://github.com/spring-projects-experimental/spring-grpc)
- [Spring Boot 3 Documentation](https://docs.spring.io/spring-boot/docs/3.0.0/reference/html/)
- [Protocol Buffers Documentation](https://developers.google.com/protocol-buffers/docs/overview)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [MongoDB Reactive Streams](https://www.mongodb.com/docs/drivers/reactive-streams/)