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

    override fun createTodo(request: TodoRequest, responseObserver: StreamObserver<TodoResponse>) {
        val todo = Todo(
            title = request.title,
            description = request.description,
            completed = request.completed,
            dueDate = request.dueDate?.toInstant()
        )
        todoRepository.save(todo)
            .map { TodoResponse.newBuilder().setTodo(it.toProto()).build() }
            .subscribe(
                { response -> responseObserver.onNext(response); responseObserver.onCompleted() },
                { error -> responseObserver.onError(error) }
            )
    }

    override fun getTodo(request: TodoIdRequest, responseObserver: StreamObserver<TodoResponse>) {
        todoRepository.findById(request.id)
            .switchIfEmpty(Mono.error(RuntimeException("Todo not found")))
            .map { TodoResponse.newBuilder().setTodo(it.toProto()).build() }
            .subscribe(
                { response -> responseObserver.onNext(response); responseObserver.onCompleted() },
                { error -> responseObserver.onError(error) }
            )
    }

    override fun listTodos(request: ListTodosRequest, responseObserver: StreamObserver<ListTodosResponse>) {
        val todos = if (request.showCompleted) {
            todoRepository.findAll()
        } else {
            todoRepository.findByCompleted(false)
        }
        todos.collectList()
            .map { items ->
                ListTodosResponse.newBuilder()
                    .addAllTodos(items.map { it.toProto() })
                    .build()
            }
            .subscribe(
                { response -> responseObserver.onNext(response); responseObserver.onCompleted() },
                { error -> responseObserver.onError(error) }
            )
    }

    override fun updateTodo(request: TodoRequest, responseObserver: StreamObserver<TodoResponse>) {
        todoRepository.findById(request.id)
            .switchIfEmpty(Mono.error(RuntimeException("Todo not found")))
            .map { existing ->
                existing.copy(
                    title = request.title,
                    description = request.description,
                    completed = request.completed,
                    dueDate = request.dueDate?.toInstant(),
                    updatedAt = Instant.now()
                )
            }
            .flatMap { todoRepository.save(it) }
            .map { TodoResponse.newBuilder().setTodo(it.toProto()).build() }
            .subscribe(
                { response -> responseObserver.onNext(response); responseObserver.onCompleted() },
                { error -> responseObserver.onError(error) }
            )
    }

    override fun deleteTodo(request: TodoIdRequest?, responseObserver: StreamObserver<Empty>?) {
        request?.let {
            todoRepository.deleteById(it.id)
                .then(Mono.just(Empty.newBuilder().build()))
                .subscribe(
                    { response -> responseObserver?.onNext(response); responseObserver?.onCompleted() },
                    { error -> responseObserver?.onError(error) }
                )
        } ?: run {
            responseObserver?.onNext(Empty.newBuilder().build())
            responseObserver?.onCompleted()
        }
    }
}

// Extension functions remain unchanged
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

fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(seconds, nanos.toLong())