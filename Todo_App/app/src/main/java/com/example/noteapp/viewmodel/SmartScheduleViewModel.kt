package com.example.noteapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteapp.appwrite.AppwriteRepository
import com.example.noteapp.model.ToDo
import com.example.noteapp.service.AIService
import com.example.noteapp.service.AIServiceFactory
import com.example.noteapp.service.AITodoRequest
import com.example.noteapp.auth.SessionManager

import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmartScheduleViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AppwriteRepository(application.applicationContext)
    private val sessionManager = SessionManager(application.applicationContext)
    private val aiServiceFactory = AIServiceFactory(application.applicationContext)

    // LiveData for UI state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _generatedTodos = MutableLiveData<List<ToDo>>()
    val generatedTodos: LiveData<List<ToDo>> = _generatedTodos
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage
    
    private val _hasApiKey = MutableLiveData<Boolean>()
    val hasApiKey: LiveData<Boolean> = _hasApiKey
    
    private val _conflictingTodos = MutableLiveData<List<ToDo>>()
    val conflictingTodos: LiveData<List<ToDo>> = _conflictingTodos
    
    // Store current generated todos for confirmation
    private var currentGeneratedTodos: List<ToDo> = emptyList()
    
    init {
        checkApiConfiguration()
    }
    
    fun checkApiConfiguration() {
        _hasApiKey.value = aiServiceFactory.hasValidApiKey()
    }
    
    fun saveApiConfiguration(apiKey: String) {
        aiServiceFactory.setGeminiKey(apiKey)
        checkApiConfiguration()
    }
    
    fun generateTodos(prompt: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = ""
                
                // Check if user is authenticated
                val currentUserId = sessionManager.getCurrentUserId()
                if (currentUserId == null) {
                    _errorMessage.value = "Please log in to use AI features"
                    return@launch
                }
                
                // Get AI service
                val aiService = aiServiceFactory.createAIService()
                if (aiService == null) {
                    _errorMessage.value = "Please configure your AI API key first"
                    return@launch
                }
                
                // Create AI request
                val currentTime = getCurrentTimeISO()
                val request = AITodoRequest(
                    prompt = prompt,
                    userId = currentUserId,
                    currentTime = currentTime
                )
                Log.d("SmartScheduleViewModel", "AI Request: $request")
                // Generate todos using AI
                val response = aiService.generateTodosFromPrompt(request)
                Log.d("SmartScheduleViewModel", "AI Response: $response")
                if (response.success && response.todos.isNotEmpty()) {
                    // Store generated todos
                    currentGeneratedTodos = response.todos
                    _generatedTodos.value = response.todos
                    
                    // Check for scheduling conflicts
                    checkForConflicts(currentUserId, response.todos)

                    _successMessage.value = ""
                } else {
                    _errorMessage.value = response.error ?: "Failed to generate todos. Please try a different prompt."
                    _generatedTodos.value = emptyList()
                }
                
            } catch (e: Exception) {
                Log.e("SmartScheduleViewModel", "Error generating todos", e)
                _errorMessage.value = "Error generating todos: ${e.message}"
                _generatedTodos.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun checkForConflicts(userId: String, newTodos: List<ToDo>) {
        try {
            val conflicts = repository.checkSchedulingConflicts(userId, newTodos)
            _conflictingTodos.value = conflicts
            
            if (conflicts.isNotEmpty()) {
                // keep them for confirmation step
                _errorMessage.value = "Warning: ${conflicts.size} task(s) may have scheduling conflicts"
            }
        } catch (e: Exception) {
            // Don't fail the whole operation for conflict checking
            _conflictingTodos.value = emptyList()
        }
    }
    
    fun confirmAndCreateTodos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = ""
                
                if (currentGeneratedTodos.isEmpty()) {
                    _errorMessage.value = "No todos to create"
                    return@launch
                }
                
                // Create todos in batch
                val createdTodos = repository.createTodosBatch(currentGeneratedTodos)
                
                if (createdTodos.isNotEmpty()) {
                    val successCount = createdTodos.size
                    val totalCount = currentGeneratedTodos.size
                    
                    _successMessage.value = if (successCount == totalCount) {
                        "Successfully created $successCount task${if (successCount != 1) "s" else ""}!"
                    } else {
                        "Created $successCount out of $totalCount tasks. Some tasks failed to create."
                    }
                    
                    // Clear generated todos after successful creation
                    currentGeneratedTodos = emptyList()
                    _generatedTodos.value = emptyList()
                    _conflictingTodos.value = emptyList()
                } else {
                    _errorMessage.value = "Failed to create any tasks. Please try again."
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Error creating tasks: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateGeneratedTodo(index: Int, updatedTodo: ToDo) {
        if (index >= 0 && index < currentGeneratedTodos.size) {
            val updatedList = currentGeneratedTodos.toMutableList()
            updatedList[index] = updatedTodo
            currentGeneratedTodos = updatedList
            _generatedTodos.value = updatedList
        }
    }
    
    fun removeGeneratedTodo(index: Int) {
        if (index >= 0 && index < currentGeneratedTodos.size) {
            val updatedList = currentGeneratedTodos.toMutableList()
            updatedList.removeAt(index)
            currentGeneratedTodos = updatedList
            _generatedTodos.value = updatedList
        }
    }
    
    fun clearError() {
        _errorMessage.value = ""
    }
    
    fun clearSuccess() {
        _successMessage.value = ""
    }
    
    private fun getCurrentTimeISO(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
    
    // Get quick prompt suggestions
    fun getQuickPromptSuggestions(): List<String> {
        return listOf(
            "Schedule a 2-hour study session for tomorrow afternoon",
            "Plan a 1-hour team meeting for next Monday at 10am",
            "Set up a 45-minute workout session for tomorrow morning",
            "Create a 30-minute reading time for tonight at 8pm",
            "Schedule a 1.5-hour project work session for Friday",
            "Plan a 20-minute meditation session for every morning this week",
            "Set up a 2-hour coding practice session for this weekend",
            "Create a 1-hour grocery shopping task for Saturday morning"
        )
    }
    
    // Validate prompt before sending to AI
    fun isValidPrompt(prompt: String): Boolean {
        val trimmed = prompt.trim()
        return trimmed.isNotEmpty() && trimmed.length >= 5
    }
    
    // Get current AI provider info
    fun getCurrentAIProvider(): String {
        return aiServiceFactory.getCurrentProvider().uppercase()
    }
}
