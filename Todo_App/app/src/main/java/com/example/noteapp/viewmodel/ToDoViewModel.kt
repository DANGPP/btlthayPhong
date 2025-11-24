package com.example.noteapp.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp.appwrite.AppwriteRepository
import com.example.noteapp.model.ToDo
import com.example.noteapp.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.noteapp.auth.SessionManager

class ToDoViewModel(private val context: Context) : ViewModel() {
    // Use applicationContext to avoid leaking an Activity/Fragment context
    private val appContext = context.applicationContext
    private val repository = AppwriteRepository(appContext)
    private val sessionManager = SessionManager(appContext)
    private val notificationRepository = NotificationRepository(appContext)

    init {
        Log.d("ToDoViewModel", "ViewModel instance created: ${this.hashCode()}")
    }
    
    private val _allTodos = MutableLiveData<List<ToDo>>()
    val allTodos: LiveData<List<ToDo>> = _allTodos
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _operationSuccess = MutableLiveData<Boolean?>()
    val operationSuccess: LiveData<Boolean?> = _operationSuccess

    private val _shouldRedirectToLogin = MutableLiveData<Boolean>()
    val shouldRedirectToLogin: LiveData<Boolean> = _shouldRedirectToLogin

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun insertTodo(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        Log.d("ToDoViewModel", "insertTodo called on ViewModel instance: ${this@ToDoViewModel.hashCode()}")
        Log.d("ToDoViewModel", "Now getting current user ID")
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            Log.e("ToDoViewModel", "User not authenticated, cannot create todo")
            _error.postValue("User not authenticated. Please login again.")
            _shouldRedirectToLogin.postValue(true)
            _isLoading.postValue(false)
            return@launch
        }
        Log.d("ToDoViewModel", "Inserting todo for userId: $userId")
        val todoToCreate = todo.copy(userId = userId)
        Log.d("ToDoViewModel", "Todo to create: $todoToCreate")
        val result = repository.createTodo(todoToCreate)
        Log.d("ToDoViewModel", "Repository createTodo result: $result")
        if (result != null) {
            Log.d("ToDoViewModel", "Todo created successfully")
            // Schedule notifications for the new todo
            notificationRepository.scheduleNotificationsForTodo(result)
            Log.d("ToDoViewModel", "About to post operationSuccess = true")
            _operationSuccess.postValue(true)
            Log.d("ToDoViewModel", "Posted operationSuccess = true")
            loadAllTodos()
        } else {
            Log.e("ToDoViewModel", "Failed to create todo - repository returned null")
            _error.postValue("Failed to create todo")
            Log.d("ToDoViewModel", "About to post operationSuccess = false")
            _operationSuccess.postValue(false)
            Log.d("ToDoViewModel", "Posted operationSuccess = false")
        }
        _isLoading.postValue(false)
    }

    fun loadAllTodos() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        Log.d("ToDoViewModel", "Loading all todos")
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            Log.e("ToDoViewModel", "User not authenticated, cannot load todos")
            _shouldRedirectToLogin.postValue(true)
            _isLoading.postValue(false)
            return@launch
        }
        Log.d("ToDoViewModel", "Loading todos for userId: $userId")
        val todos = repository.getAllTodosByUserId(userId)
        Log.d("ToDoViewModel", "Fetched ${todos.size} todos from repository")
        Log.d("ToDoViewModel", "About to post todos to LiveData: $todos")
        _allTodos.postValue(todos)
        Log.d("ToDoViewModel", "Posted todos to LiveData")
        _isLoading.postValue(false)
    }

    fun updateTodo(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val result = repository.updateTodo(todo.id, todo)
        if (result != null) {
            // Update notifications for the updated todo
            notificationRepository.cancelNotificationsForTodo(todo.id)
            notificationRepository.scheduleNotificationsForTodo(result)
            showToast("Todo updated")
            _operationSuccess.postValue(true)
            loadAllTodos()
        } else {
            showToast("Failed to update todo")
            _operationSuccess.postValue(false)
        }
        _isLoading.postValue(false)
    }

    fun deleteTodo(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val result = repository.deleteTodo(todo.id)
        if (result) {
            // Cancel notifications for the deleted todo
            notificationRepository.cancelNotificationsForTodo(todo.id)
            showToast("Todo deleted")
            _operationSuccess.postValue(true)
            loadAllTodos()
        } else {
            showToast("Failed to delete todo")
            _operationSuccess.postValue(false)
        }
        _isLoading.postValue(false)
    }

    fun loadAllTodosSortedByCreatedTimeASC() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _shouldRedirectToLogin.postValue(true)
            _isLoading.postValue(false)
            return@launch
        }
        val todos = repository.getAllTodosSortedByCreatedTimeASC(userId)
        _allTodos.postValue(todos)
        _isLoading.postValue(false)
    }

    fun loadAllTodosSortedByCreatedTimeDESC() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _shouldRedirectToLogin.postValue(true)
            _isLoading.postValue(false)
            return@launch
        }
        val todos = repository.getAllTodosSortedByCreatedTimeDESC(userId)
        _allTodos.postValue(todos)
        _isLoading.postValue(false)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearOperationSuccess() {
        _operationSuccess.postValue(null)
    }

    fun clearRedirectToLogin() {
        _shouldRedirectToLogin.value = false
    }

    fun searchTodos(searchQuery: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _shouldRedirectToLogin.postValue(true)
            _isLoading.postValue(false)
            return@launch
        }
        val todos = repository.searchTodos(userId, searchQuery)
        _allTodos.postValue(todos)
        _isLoading.postValue(false)
    }

    fun getTodosByCompletionStatus(isCompleted: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _shouldRedirectToLogin.postValue(true)
            _isLoading.postValue(false)
            return@launch
        }
        val todos = repository.getTodosByCompletionStatus(userId, isCompleted)
        _allTodos.postValue(todos)
        _isLoading.postValue(false)
    }

    fun getTodosByPriority(priority: com.example.noteapp.model.TodoPriority) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _shouldRedirectToLogin.postValue(true)
            _isLoading.postValue(false)
            return@launch
        }
        val todos = repository.getTodosByPriority(userId, priority)
        _allTodos.postValue(todos)
        _isLoading.postValue(false)
    }

    fun getTodosByCategory(category: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _shouldRedirectToLogin.postValue(true)
            _isLoading.postValue(false)
            return@launch
        }
        val todos = repository.getTodosByCategory(userId, category)
        _allTodos.postValue(todos)
        _isLoading.postValue(false)
    }

    fun getTodosDueToday(todayDate: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _shouldRedirectToLogin.postValue(true)
            _isLoading.postValue(false)
            return@launch
        }
        val todos = repository.getTodosDueToday(userId, todayDate)
        _allTodos.postValue(todos)
        _isLoading.postValue(false)
    }

    fun updateTodoStatus(todo: ToDo, newStatus: com.example.noteapp.model.TodoStatus) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val currentTime = java.text.SimpleDateFormat("dd/M/yyyy hh:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val updatedTodo = todo.copy(
            status = newStatus,
            completedDate = if (newStatus == com.example.noteapp.model.TodoStatus.COMPLETED) currentTime else todo.completedDate
        )
        val result = repository.updateTodo(todo.id, updatedTodo)
        if (result != null) {
            showToast("Todo status updated to ${newStatus.displayName}")
            _operationSuccess.postValue(true)
            loadAllTodos()
        } else {
            showToast("Failed to update todo status")
            _operationSuccess.postValue(false)
        }
        _isLoading.postValue(false)
    }

    fun markTodoCompleted(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        updateTodoStatus(todo, com.example.noteapp.model.TodoStatus.COMPLETED)
    }

    fun markTodoIncomplete(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        updateTodoStatus(todo, com.example.noteapp.model.TodoStatus.TODO)
    }

    fun markTodoInProgress(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        updateTodoStatus(todo, com.example.noteapp.model.TodoStatus.IN_PROGRESS)
    }

    fun markTodoOnHold(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        updateTodoStatus(todo, com.example.noteapp.model.TodoStatus.ON_HOLD)
    }

    fun markTodoCancelled(todo: ToDo) = viewModelScope.launch(Dispatchers.IO) {
        updateTodoStatus(todo, com.example.noteapp.model.TodoStatus.CANCELLED)
    }

    fun getTodosByStatus(status: com.example.noteapp.model.TodoStatus) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _shouldRedirectToLogin.postValue(true)
            _isLoading.postValue(false)
            return@launch
        }
        val todos = repository.getTodosByStatus(userId, status)
        _allTodos.postValue(todos)
        _isLoading.postValue(false)
    }

    class ToDoViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ToDoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ToDoViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}