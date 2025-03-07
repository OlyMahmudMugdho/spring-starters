package com.mahmud.grpcserver.repository

import com.mahmud.grpcserver.model.Todo
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux


interface TodoRepository : ReactiveMongoRepository<Todo, String> {
    fun findByCompleted(completed: Boolean): Flux<Todo>
}