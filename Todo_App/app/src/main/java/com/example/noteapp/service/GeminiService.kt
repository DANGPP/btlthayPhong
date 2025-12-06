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

class GeminiService(private val apiKey: String) : AIService {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    
    override suspend fun generateTodosFromPrompt(request: AITodoRequest): AITodoResponse {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = createGeminiPrompt(request)
                val requestBody = createGeminiRequest(prompt)
                
                val httpRequest = Request.Builder()
                    .url("$baseUrl?key=$apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()
                
                val response = client.newCall(httpRequest).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    parseGeminiResponse(responseBody, request.userId, request.currentTime)
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
    
    private fun createGeminiPrompt(request: AITodoRequest): String {
        return """
            You are an AI assistant that converts natural language requests into structured todo items.
            
            Current time: ${request.currentTime}
            User request: "${request.prompt}"
            
            Extract the following information and respond ONLY with valid JSON array:
            - title: Main task description
            - description: Additional details (optional)
            - dueTime: When the task should be completed (ISO format or null)
            - estimatedDuration: How long the task will take in minutes
            - priority: LOW, MEDIUM, HIGH, or URGENT
            - category: Subject/type of task (e.g., Study, Work, Personal, Health)
            - reminderTime: When to remind user (ISO format or null)
            
            Parse relative dates like "tomorrow", "next Tuesday", "in 2 hours" based on current time.
            
            Example response:
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
    
    private fun createGeminiRequest(prompt: String): RequestBody {
        val jsonObject = JsonObject().apply {
            val contents = com.google.gson.JsonArray().apply {
                add(JsonObject().apply {
                    val parts = com.google.gson.JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("text", prompt)
                        })
                    }
                    add("parts", parts)
                })
            }
            add("contents", contents)
            
            val generationConfig = JsonObject().apply {
                addProperty("temperature", 0.3)
                addProperty("maxOutputTokens", 1000)
            }
            add("generationConfig", generationConfig)
        }
        
        return jsonObject.toString().toRequestBody("application/json".toMediaType())
    }
    
    private fun parseGeminiResponse(responseBody: String?, userId: String, currentTime: String): AITodoResponse {
        return try {
            if (responseBody == null) {
                return AITodoResponse(false, emptyList(), "Empty response")
            }
            
            val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
            val candidates = jsonResponse.getAsJsonArray("candidates")
            
            if (candidates.size() == 0) {
                return AITodoResponse(false, emptyList(), "No candidates in response")
            }
            
            val content = candidates[0].asJsonObject
                .getAsJsonObject("content")
                .getAsJsonArray("parts")[0].asJsonObject
                .get("text").asString
            
            // Clean up the response to extract JSON
            val jsonContent = content.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()
            
            // Parse the JSON array from AI response
            val todoArray = gson.fromJson(jsonContent, Array<AITodoData>::class.java)
            
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
