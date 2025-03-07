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
