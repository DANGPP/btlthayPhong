package com.example.noteapp.service

import com.example.noteapp.model.ToDo

data class AITodoRequest(
    val prompt: String,
    val userId: String,
    val currentTime: String
)

data class AITodoResponse(
    val success: Boolean,
    val todos: List<ToDo>,
    val error: String? = null
)

interface AIService {
    suspend fun generateTodosFromPrompt(request: AITodoRequest): AITodoResponse
}
