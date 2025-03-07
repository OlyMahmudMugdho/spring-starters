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

@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoClientService) {

    @PostMapping
    fun createTodo(
        @RequestBody request: TodoRequestDto
    ): Mono<TodoItemDto> {
        val todo = todoService.createTodo(
            request.title,
            request.description,
            request.completed,
            request.dueDate?.let { Instant.ofEpochSecond(it) }
        )
         return Mono.justOrEmpty(todo?.let { mapToTodoItemDto(it) })
    }

    @GetMapping("/{id}")
    fun getTodo(@PathVariable id: String): Mono<TodoItemDto> {
        return Mono.justOrEmpty(mapToTodoItemDto(todoService.getTodo(id)))
    }

    @GetMapping
    fun listTodos(@RequestParam(defaultValue = "true") showCompleted: Boolean): Flux<TodoItemDto> {
        val todoList = todoService.listTodos(showCompleted)
        return todoList?.let { Flux.fromIterable(it).map { todoItem -> mapToTodoItemDto(todoItem) } } ?: Flux.empty()
    }

    @PutMapping("/{id}")
    fun updateTodo(
        @PathVariable id: String,
        @RequestBody request: TodoRequestDto
    ): Mono<TodoItemDto> {
        val todo = todoService.updateTodo(
            id,
            request.title,
            request.description,
            request.completed,
            request.dueDate?.let { Instant.ofEpochSecond(it) }
        )
        return Mono.justOrEmpty(todo?.let { mapToTodoItemDto(it) })
    }

    @DeleteMapping("/{id}")
    fun deleteTodo(@PathVariable id: String): Empty? {
        return todoService.deleteTodo(id)
    }
}