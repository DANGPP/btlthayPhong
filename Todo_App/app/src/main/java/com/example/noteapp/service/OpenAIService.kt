package com.example.noteapp.service

import com.example.noteapp.model.ToDo
import com.example.noteapp.model.TodoPriority
import com.example.noteapp.model.TodoStatus
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class OpenAIService(private val apiKey: String) : AIService {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://api.openai.com/v1/chat/completions"
    
    override suspend fun generateTodosFromPrompt(request: AITodoRequest): AITodoResponse {
        return withContext(Dispatchers.IO) {
            try {
                val systemPrompt = createSystemPrompt()
                val userPrompt = createUserPrompt(request)
                
                val requestBody = createOpenAIRequest(systemPrompt, userPrompt)
                val httpRequest = Request.Builder()
                    .url(baseUrl)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()
                
                val response = client.newCall(httpRequest).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    parseOpenAIResponse(responseBody, request.userId, request.currentTime)
                } else {
                    AITodoResponse(
                        success = false,
                        todos = emptyList(),
                        error = "API Error: ${response.code} ${response.message}"
                    )
                }
            } catch (e: Exception) {
                AITodoResponse(
                    success = false,
                    todos = emptyList(),
                    error = "Network Error: ${e.message}"
                )
            }
        }
    }
    
    private fun createSystemPrompt(): String {
        return """
            You are an AI assistant that converts natural language requests into structured todo items.
            
            Extract the following information from user prompts:
            - title: Main task description
            - description: Additional details (optional)
            - dueTime: When the task should be completed (ISO format or null)
            - estimatedDuration: How long the task will take in minutes
            - priority: LOW, MEDIUM, HIGH, or URGENT
            - category: Subject/type of task (e.g., Study, Work, Personal, Health)
            - reminderTime: When to remind user (ISO format or null)
            
            Current date/time context will be provided. Parse relative dates like "tomorrow", "next Tuesday", "in 2 hours".
            
            Respond ONLY with valid JSON array of todo objects. Example:
            [
              {
                "title": "Study Economics",
                "description": "Review chapters 5-7 for upcoming exam",
                "dueTime": "2024-01-16T14:00:00Z",
                "estimatedDuration": 120,
                "priority": "HIGH",
                "category": "Study",
                "reminderTime": "2024-01-16T13:30:00Z"
              }
            ]
            
            If you cannot parse the request, return empty array: []
        """.trimIndent()
    }
    
    private fun createUserPrompt(request: AITodoRequest): String {
        return """
            Current time: ${request.currentTime}
            User request: "${request.prompt}"
            
            Convert this to todo items.
        """.trimIndent()
    }
    
    private fun createOpenAIRequest(systemPrompt: String, userPrompt: String): RequestBody {
        val jsonObject = JsonObject().apply {
            addProperty("model", "gpt-3.5-turbo")
            addProperty("max_tokens", 1000)
            addProperty("temperature", 0.3)
            
            val messages = com.google.gson.JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "system")
                    addProperty("content", systemPrompt)
                })
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", userPrompt)
                })
            }
            add("messages", messages)
        }
        
        return jsonObject.toString().toRequestBody("application/json".toMediaType())
    }
    
    private fun parseOpenAIResponse(responseBody: String?, userId: String, currentTime: String): AITodoResponse {
        return try {
            if (responseBody == null) {
                return AITodoResponse(false, emptyList(), "Empty response")
            }
            
            val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
            val choices = jsonResponse.getAsJsonArray("choices")
            
            if (choices.size() == 0) {
                return AITodoResponse(false, emptyList(), "No choices in response")
            }
            
            val content = choices[0].asJsonObject
                .getAsJsonObject("message")
                .get("content").asString
            
            // Parse the JSON array from AI response
            val todoArray = gson.fromJson(content, Array<AITodoData>::class.java)
            
            val todos = todoArray.map { aiTodo ->
                ToDo(
                    title = aiTodo.title,
                    description = aiTodo.description ?: "",
                    createdTime = currentTime,
                    dueTime = aiTodo.dueTime,
                    completedDate = null,
                    userId = userId,
                    status = TodoStatus.TODO,
                    priority = parsePriority(aiTodo.priority),
                    category = aiTodo.category ?: "General",
                    reminderTime = aiTodo.reminderTime,
                    estimatedDuration = aiTodo.estimatedDuration ?: 0,
                    actualDuration = 0
                )
            }
            
            AITodoResponse(true, todos)
            
        } catch (e: Exception) {
            AITodoResponse(false, emptyList(), "Parse Error: ${e.message}")
        }
    }
    
    private fun parsePriority(priority: String?): TodoPriority {
        return when (priority?.uppercase()) {
            "LOW" -> TodoPriority.LOW
            "MEDIUM" -> TodoPriority.MEDIUM
            "HIGH" -> TodoPriority.HIGH
            "URGENT" -> TodoPriority.URGENT
            else -> TodoPriority.MEDIUM
        }
    }
    
    private data class AITodoData(
        val title: String,
        val description: String?,
        val dueTime: String?,
        val estimatedDuration: Int?,
        val priority: String?,
        val category: String?,
        val reminderTime: String?
    )
}
