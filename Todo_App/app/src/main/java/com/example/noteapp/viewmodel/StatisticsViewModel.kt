package com.example.noteapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp.model.*
import com.example.noteapp.repository.StatisticsRepository
import com.example.noteapp.appwrite.AuthService
import kotlinx.coroutines.launch

class StatisticsViewModel(private val context: android.content.Context) : ViewModel() {
    private val statisticsRepository = StatisticsRepository()
    private val authService = AuthService(context)
    
    private val _todoStatistics = MutableLiveData<TodoStatistics>()
    val todoStatistics: LiveData<TodoStatistics> = _todoStatistics
    
    private val _overallStatistics = MutableLiveData<OverallStatistics>()
    val overallStatistics: LiveData<OverallStatistics> = _overallStatistics
    
    private val _weeklyReport = MutableLiveData<WeeklyReport>()
    val weeklyReport: LiveData<WeeklyReport> = _weeklyReport
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    init {
        loadStatistics()
    }
    
    fun loadStatistics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = authService.getCurrentUserId()
                if (userId != null) {
                    
                    // Load all statistics
                    val todoStats = statisticsRepository.getTodoStatistics(userId)
                    _todoStatistics.value = todoStats
                    
                    val overallStats = statisticsRepository.getOverallStatistics(userId)
                    _overallStatistics.value = overallStats
                    
                    val weeklyReport = statisticsRepository.getWeeklyReport(userId)
                    _weeklyReport.value = weeklyReport
                    
                } else {
                    _error.value = "User not authenticated"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load statistics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshStatistics() {
        loadStatistics()
    }
    
    class StatisticsViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StatisticsViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
