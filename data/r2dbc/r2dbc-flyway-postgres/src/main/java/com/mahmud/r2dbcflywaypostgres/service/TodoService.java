package com.mahmud.r2dbcflywaypostgres.service;

import com.mahmud.r2dbcflywaypostgres.domain.Todo;
import com.mahmud.r2dbcflywaypostgres.repository.TodoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TodoService {
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public Flux<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Flux<Todo> getTodosByCompleted(boolean completed) {
        return todoRepository.findByCompleted(completed);
    }

    public Mono<Todo> getTodoById(Long id) {
        return todoRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Todo not found")));
    }

    public Mono<Todo> createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    public Mono<Todo> updateTodo(Long id, Todo todo) {
        return todoRepository.findById(id)
                .flatMap(existingTodo -> {
                    existingTodo.setTitle(todo.getTitle());
                    existingTodo.setDescription(todo.getDescription());
                    existingTodo.setCompleted(todo.getCompleted());
                    existingTodo.setPriority(todo.getPriority());
                    return todoRepository.save(existingTodo);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Todo not found")));
    }

    public Mono<Void> deleteTodo(Long id) {
        return todoRepository.deleteById(id);
    }
}