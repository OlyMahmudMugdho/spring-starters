package com.mahmud.r2dbcflywaypostgres.repository;

import com.mahmud.r2dbcflywaypostgres.domain.Todo;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface TodoRepository extends R2dbcRepository<Todo, Long> {
    Flux<Todo> findByCompleted(Boolean completed);
}