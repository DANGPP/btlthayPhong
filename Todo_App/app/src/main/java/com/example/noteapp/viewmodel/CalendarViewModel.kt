package com.example.noteapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp.appwrite.AppwriteRepository
import com.example.noteapp.auth.SessionManager
import com.example.noteapp.model.ToDo
import com.example.noteapp.model.TodoStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewModel(
    private val repository: AppwriteRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "CalendarViewModel"
    }
    
    private val _todosForDate = MutableLiveData<List<ToDo>>()
    val todosForDate: LiveData<List<ToDo>> = _todosForDate
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    private fun extractDateFromDateTime(dateTimeString: String?): String? {
        if (dateTimeString == null) return null
        return try {
            // If it's already in date format (dd/MM/yyyy), return as is
            if (dateTimeString.matches(Regex("\\d{2}/\\d{2}/\\d{4}$"))) {
                dateTimeString
            } else {
                // Extract date part from datetime string (dd/MM/yyyy HH:mm)
                dateTimeString.split(" ")[0]
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error extracting date from: $dateTimeString", e)
            null
        }
    }
    
    fun loadTodosForDate(dateString: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Loading todos for date: $dateString")
                
                val userId = sessionManager.getCurrentUserId()
                if (userId == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }
                
                val allTodos = repository.getAllTodosByUserId(userId)
                val todosForDate = allTodos.filter { todo ->
                    // Extract date parts and compare
                    val createdDate = extractDateFromDateTime(todo.createdTime)
                    val dueDate = extractDateFromDateTime(todo.dueTime)
                    val reminderDate = extractDateFromDateTime(todo.reminderTime)
                    
                    Log.d(TAG, "Todo: ${todo.title}, createdDate: $createdDate, dueDate: $dueDate, reminderDate: $reminderDate, target: $dateString")
                    
                    createdDate == dateString || dueDate == dateString || reminderDate == dateString
                }
                
                Log.d(TAG, "Found ${todosForDate.size} todos for date $dateString")
                _todosForDate.value = todosForDate
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading todos for date", e)
                _error.value = "Failed to load todos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadTodosForDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Loading todos for date range: $startDate to $endDate")
                
                val userId = sessionManager.getCurrentUserId()
                if (userId == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }
                
                val allTodos = repository.getAllTodosByUserId(userId)
                val todosInRange = allTodos.filter { todo ->
                    val createdDate = extractDateFromDateTime(todo.createdTime)
                    val dueDate = extractDateFromDateTime(todo.dueTime)
                    
                    (createdDate != null && isDateInRange(createdDate, startDate, endDate)) ||
                    (dueDate != null && isDateInRange(dueDate, startDate, endDate))
                }
                
                Log.d(TAG, "Found ${todosInRange.size} todos in date range")
                _todosForDate.value = todosInRange
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading todos for date range", e)
                _error.value = "Failed to load todos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateTodoStatus(todo: ToDo, newStatus: TodoStatus) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating todo ${todo.id} status to ${newStatus.value}")
                
                val updatedTodo = todo.copy(
                    status = newStatus,
                    completedDate = if (newStatus == TodoStatus.COMPLETED) {
                        dateFormatter.format(Date())
                    } else null
                )
                
                val result = repository.updateTodo(todo.id, updatedTodo)
                if (result != null) {
                    // Refresh the current date's todos
                    val currentTodos = _todosForDate.value?.toMutableList() ?: mutableListOf()
                    val index = currentTodos.indexOfFirst { it.id == todo.id }
                    if (index != -1) {
                        currentTodos[index] = result
                        _todosForDate.value = currentTodos
                    }
                    Log.d(TAG, "Todo status updated successfully")
                } else {
                    _error.value = "Failed to update todo status"
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error updating todo status", e)
                _error.value = "Failed to update todo: ${e.message}"
            }
        }
    }
    
    fun getTodosForWeek(weekStartDate: String): LiveData<Map<String, List<ToDo>>> {
        val weekTodos = MutableLiveData<Map<String, List<ToDo>>>()
        
        viewModelScope.launch {
            try {
                val userId = sessionManager.getCurrentUserId()
                if (userId == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }
                
                val calendar = Calendar.getInstance()
                val startDate = dateFormatter.parse(weekStartDate)
                calendar.time = startDate ?: Date()
                
                val weekMap = mutableMapOf<String, List<ToDo>>()
                
                // Get todos for each day of the week
                for (i in 0..6) {
                    val dayString = dateFormatter.format(calendar.time)
                    val allTodos = repository.getAllTodosByUserId(userId)
                    val dayTodos = allTodos.filter { todo ->
                        val createdDate = extractDateFromDateTime(todo.createdTime)
                        val dueDate = extractDateFromDateTime(todo.dueTime)
                        
                        createdDate == dayString || dueDate == dayString
                    }
                    
                    weekMap[dayString] = dayTodos
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                weekTodos.value = weekMap
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading week todos", e)
                _error.value = "Failed to load week data: ${e.message}"
            }
        }
        
        return weekTodos
    }
    
    fun getTodosForMonth(monthYear: String): LiveData<Map<String, List<ToDo>>> {
        val monthTodos = MutableLiveData<Map<String, List<ToDo>>>()
        
        viewModelScope.launch {
            try {
                val userId = sessionManager.getCurrentUserId()
                if (userId == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }
                
                val allTodos = repository.getAllTodosByUserId(userId)
                val monthMap = mutableMapOf<String, List<ToDo>>()
                
                allTodos.forEach { todo ->
                    val createdDate = extractDateFromDateTime(todo.createdTime)
                    val dueDate = extractDateFromDateTime(todo.dueTime)
                    
                    val todoMonth = createdDate?.let { extractMonthYear(it) }
                    val dueDateMonth = dueDate?.let { extractMonthYear(it) }
                    
                    if (todoMonth == monthYear && createdDate != null) {
                        val dayTodos = monthMap[createdDate] ?: emptyList()
                        monthMap[createdDate] = dayTodos + todo
                    }
                    
                    if (dueDateMonth == monthYear && dueDate != null && dueDate != createdDate) {
                        val dayTodos = monthMap[dueDate] ?: emptyList()
                        monthMap[dueDate] = dayTodos + todo
                    }
                }
                
                monthTodos.value = monthMap
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading month todos", e)
                _error.value = "Failed to load month data: ${e.message}"
            }
        }
        
        return monthTodos
    }
    
    private fun isDateInRange(dateString: String, startDate: String, endDate: String): Boolean {
        return try {
            val date = dateFormatter.parse(dateString)
            val start = dateFormatter.parse(startDate)
            val end = dateFormatter.parse(endDate)
            
            date != null && start != null && end != null &&
            (date.equals(start) || date.after(start)) &&
            (date.equals(end) || date.before(end))
        } catch (e: Exception) {
            false
        }
    }
    
    private fun extractMonthYear(dateString: String): String {
        return try {
            val date = dateFormatter.parse(dateString)
            val monthYearFormatter = SimpleDateFormat("MM/yyyy", Locale.getDefault())
            monthYearFormatter.format(date ?: Date())
        } catch (e: Exception) {
            ""
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    class CalendarViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
                val repository = AppwriteRepository(context)
                val sessionManager = SessionManager(context.applicationContext)
                @Suppress("UNCHECKED_CAST")
                return CalendarViewModel(repository, sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
