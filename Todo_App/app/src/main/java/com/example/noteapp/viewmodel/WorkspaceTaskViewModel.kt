package com.example.noteapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp.model.TodoStatus
import com.example.noteapp.model.WorkspaceTask
import com.example.noteapp.repository.WorkspaceTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkspaceTaskViewModel(context: Context) : ViewModel() {
    
    private val repository = WorkspaceTaskRepository(context)
    
    private val _tasks = MutableLiveData<List<WorkspaceTask>>()
    val tasks: LiveData<List<WorkspaceTask>> = _tasks
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _operationSuccess = MutableLiveData<String?>()
    val operationSuccess: LiveData<String?> = _operationSuccess
    
    // Load all tasks in workspace
    fun loadWorkspaceTasks(workspaceId: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val result = repository.getWorkspaceTasks(workspaceId)
            result.onSuccess { tasks ->
                _tasks.postValue(tasks)
            }.onFailure { exception ->
                _error.postValue(exception.message ?: "Lỗi khi tải công việc")
            }
        } catch (e: Exception) {
            _error.postValue(e.message ?: "Lỗi không xác định")
        } finally {
            _isLoading.postValue(false)
        }
    }
    
    // Load tasks assigned to user
    fun loadMyTasks(workspaceId: String, userId: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val result = repository.getMyTasks(workspaceId, userId)
            result.onSuccess { tasks ->
                _tasks.postValue(tasks)
            }.onFailure { exception ->
                _error.postValue(exception.message ?: "Lỗi khi tải công việc của bạn")
            }
        } catch (e: Exception) {
            _error.postValue(e.message ?: "Lỗi không xác định")
        } finally {
            _isLoading.postValue(false)
        }
    }
    
    // Create new task
    fun createTask(task: WorkspaceTask) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val result = repository.createTask(task)
            result.onSuccess {
                _operationSuccess.postValue("Đã tạo công việc mới")
                // Reload tasks
                loadWorkspaceTasks(task.workspaceId)
            }.onFailure { exception ->
                _error.postValue(exception.message ?: "Lỗi khi tạo công việc")
            }
        } catch (e: Exception) {
            _error.postValue(e.message ?: "Lỗi không xác định")
        } finally {
            _isLoading.postValue(false)
        }
    }
    
    // Update task
    fun updateTask(task: WorkspaceTask) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val result = repository.updateTask(task)
            result.onSuccess {
                _operationSuccess.postValue("Đã cập nhật công việc")
                // Reload tasks
                loadWorkspaceTasks(task.workspaceId)
            }.onFailure { exception ->
                _error.postValue(exception.message ?: "Lỗi khi cập nhật công việc")
            }
        } catch (e: Exception) {
            _error.postValue(e.message ?: "Lỗi không xác định")
        }
    }
    
    // Update task status
    fun updateTaskStatus(taskId: String, workspaceId: String, status: TodoStatus) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val result = repository.updateTaskStatus(taskId, status)
            result.onSuccess {
                _operationSuccess.postValue("Đã cập nhật trạng thái")
                // Reload tasks
                loadWorkspaceTasks(workspaceId)
            }.onFailure { exception ->
                _error.postValue(exception.message ?: "Lỗi khi cập nhật trạng thái")
            }
        } catch (e: Exception) {
            _error.postValue(e.message ?: "Lỗi không xác định")
        }
    }
    
    // Delete task
    fun deleteTask(taskId: String, workspaceId: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val result = repository.deleteTask(taskId)
            result.onSuccess {
                _operationSuccess.postValue("Đã xóa công việc")
                // Reload tasks
                loadWorkspaceTasks(workspaceId)
            }.onFailure { exception ->
                _error.postValue(exception.message ?: "Lỗi khi xóa công việc")
            }
        } catch (e: Exception) {
            _error.postValue(e.message ?: "Lỗi không xác định")
        }
    }
    
    // Load tasks by status
    fun loadTasksByStatus(workspaceId: String, status: TodoStatus) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val result = repository.getTasksByStatus(workspaceId, status)
            result.onSuccess { tasks ->
                _tasks.postValue(tasks)
            }.onFailure { exception ->
                _error.postValue(exception.message ?: "Lỗi khi tải công việc")
            }
        } catch (e: Exception) {
            _error.postValue(e.message ?: "Lỗi không xác định")
        } finally {
            _isLoading.postValue(false)
        }
    }
    
    fun clearError() {
        _error.postValue(null)
    }
    
    fun clearOperationSuccess() {
        _operationSuccess.postValue(null)
    }
    
    class WorkspaceTaskViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WorkspaceTaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WorkspaceTaskViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
