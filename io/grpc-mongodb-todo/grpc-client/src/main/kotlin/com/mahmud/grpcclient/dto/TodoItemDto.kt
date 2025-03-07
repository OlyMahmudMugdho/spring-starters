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