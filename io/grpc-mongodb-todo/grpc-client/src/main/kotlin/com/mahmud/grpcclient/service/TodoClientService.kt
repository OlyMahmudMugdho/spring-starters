package com.mahmud.grpcclient.service

import com.google.protobuf.Timestamp
import com.mahmud.grpcclient.*
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TodoClientService(private val todoStub: TodoServiceGrpc.TodoServiceBlockingStub) {

    fun createTodo(title: String, description: String, completed: Boolean, dueDate: Instant?): TodoItem? {
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
        return todoStub.createTodo(request).todo;
    }

    fun getTodo(id: String): TodoItem {
        val request = TodoIdRequest.newBuilder().setId(id).build()
        return todoStub.getTodo(request).todo
    }

    fun listTodos(showCompleted: Boolean): MutableList<TodoItem>? {
        val request = ListTodosRequest.newBuilder()
            .setShowCompleted(showCompleted)
            .build()
        return todoStub.listTodos(request).todosList
    }

    fun updateTodo(id: String, title: String, description: String, completed: Boolean, dueDate: Instant?): TodoItem? {
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
        return todoStub.updateTodo(request).todo
    }

    fun deleteTodo(id: String): Empty? {
        val request = TodoIdRequest.newBuilder().setId(id).build()
        return todoStub.deleteTodo(request)
    }
}