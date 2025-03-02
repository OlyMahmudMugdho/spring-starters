package com.mahmud.r2dbcflywaypostgres.controller;

import com.mahmud.r2dbcflywaypostgres.domain.Todo;
import com.mahmud.r2dbcflywaypostgres.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public Flux<Todo> getAllTodos() {
        return todoService.getAllTodos();
    }

    @GetMapping("/completed/{completed}")
    public Flux<Todo> getTodosByCompleted(@PathVariable boolean completed) {
        return todoService.getTodosByCompleted(completed);
    }

    @GetMapping("/{id}")
    public Mono<Todo> getTodoById(@PathVariable Long id) {
        return todoService.getTodoById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Todo> createTodo(@RequestBody Todo todo) {
        return todoService.createTodo(todo);
    }

    @PutMapping("/{id}")
    public Mono<Todo> updateTodo(@PathVariable Long id, @RequestBody Todo todo) {
        return todoService.updateTodo(id, todo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteTodo(@PathVariable Long id) {
        return todoService.deleteTodo(id);
    }
}