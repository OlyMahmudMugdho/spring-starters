package com.mahmud.grpcclient.dto

data class TodoRequestDto(
    val title: String,
    val description: String,
    val completed: Boolean = false,
    val dueDate: Long? = null // Epoch seconds
)