package com.example.noteapp.viewmodel

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp.appwrite.AppwriteRepository
import com.example.noteapp.auth.SessionManager
import com.example.noteapp.model.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PomodoroViewModel(
    private val repository: AppwriteRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        private const val TAG = "PomodoroViewModel"
    }

    // Timer State
    private val _timerState = MutableLiveData<TimerState>()
    val timerState: LiveData<TimerState> = _timerState

    private val _timeRemaining = MutableLiveData<Long>()
    val timeRemaining: LiveData<Long> = _timeRemaining

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress

    private val _currentSessionType = MutableLiveData<PomodoroSessionType>()
    val currentSessionType: LiveData<PomodoroSessionType> = _currentSessionType

    // Settings and Stats
    private val _settings = MutableLiveData<PomodoroSettings>()
    val settings: LiveData<PomodoroSettings> = _settings

    private val _stats = MutableLiveData<PomodoroStats>()
    val stats: LiveData<PomodoroStats> = _stats

    // Linked Todo
    private val _linkedTodo = MutableLiveData<ToDo?>()
    val linkedTodo: LiveData<ToDo?> = _linkedTodo

    // Error and Loading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Internal timer management
    private var countDownTimer: CountDownTimer? = null
    private var currentSession: PomodoroSession? = null
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    init {
        initializeDefaults()
        loadTodaysStats()
    }

    private fun initializeDefaults() {
        val defaultSettings = PomodoroSettings()
        _settings.value = defaultSettings
        _currentSessionType.value = PomodoroSessionType.WORK
        _timerState.value = TimerState.STOPPED
        _timeRemaining.value = defaultSettings.workDuration
        _progress.value = 0
        _stats.value = PomodoroStats()
    }

    fun startTimer() {
        val currentSettings = _settings.value ?: return
        val sessionType = _currentSessionType.value ?: PomodoroSessionType.WORK
        val duration = when (sessionType) {
            PomodoroSessionType.WORK -> currentSettings.workDuration
            PomodoroSessionType.SHORT_BREAK -> currentSettings.shortBreakDuration
            PomodoroSessionType.LONG_BREAK -> currentSettings.longBreakDuration
        }

        startSession(sessionType, duration)
    }

    private fun startSession(sessionType: PomodoroSessionType, duration: Long) {
        viewModelScope.launch {
            try {
                // Get current user id from session manager
                val currentUserId = sessionManager.getCurrentUserId()
                if (currentUserId == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }

                // Create new session
                currentSession = PomodoroSession(
                    userId = currentUserId,
                    sessionType = sessionType,
                    duration = duration,
                    startTime = Date(),
                    linkedTodoId = _linkedTodo.value?.id
                )

                _timerState.value = TimerState.RUNNING
                _timeRemaining.value = duration

                // Start countdown timer
                countDownTimer = object : CountDownTimer(duration, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        _timeRemaining.value = millisUntilFinished
                        val totalDuration = duration
                        val elapsed = totalDuration - millisUntilFinished
                        val progressPercent = ((elapsed.toFloat() / totalDuration.toFloat()) * 100).toInt()
                        _progress.value = progressPercent
                    }

                    override fun onFinish() {
                        completeSession()
                    }
                }.start()

                Log.d(TAG, "Started ${sessionType.displayName} session for ${duration / 1000} seconds")

            } catch (e: Exception) {
                Log.e(TAG, "Error starting timer session", e)
                _error.value = "Failed to start session: ${e.message}"
            }
        }
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _timerState.value = TimerState.PAUSED
        Log.d(TAG, "Timer paused")
    }

    fun resumeTimer() {
        val remainingTime = _timeRemaining.value ?: return
        val sessionType = _currentSessionType.value ?: return

        _timerState.value = TimerState.RUNNING

        countDownTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished
                val currentSettings = _settings.value ?: return
                val totalDuration = when (sessionType) {
                    PomodoroSessionType.WORK -> currentSettings.workDuration
                    PomodoroSessionType.SHORT_BREAK -> currentSettings.shortBreakDuration
                    PomodoroSessionType.LONG_BREAK -> currentSettings.longBreakDuration
                }
                val elapsed = totalDuration - millisUntilFinished
                val progressPercent = ((elapsed.toFloat() / totalDuration.toFloat()) * 100).toInt()
                _progress.value = progressPercent
            }

            override fun onFinish() {
                completeSession()
            }
        }.start()

        Log.d(TAG, "Timer resumed")
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        currentSession = null
        _timerState.value = TimerState.STOPPED
        _progress.value = 0

        val currentSettings = _settings.value ?: return
        val sessionType = _currentSessionType.value ?: PomodoroSessionType.WORK
        val duration = when (sessionType) {
            PomodoroSessionType.WORK -> currentSettings.workDuration
            PomodoroSessionType.SHORT_BREAK -> currentSettings.shortBreakDuration
            PomodoroSessionType.LONG_BREAK -> currentSettings.longBreakDuration
        }
        _timeRemaining.value = duration

        Log.d(TAG, "Timer reset")
    }

    private fun completeSession() {
        viewModelScope.launch {
            try {
                val session = currentSession ?: return@launch
                val completedSession = session.copy(
                    endTime = Date(),
                    isCompleted = true
                )

                // Save session to database (if implementing session history)
                // repository.savePomodoroSession(completedSession)

                // Update stats
                updateStatsAfterCompletion(completedSession)

                // Determine next session type
                val nextSessionType = determineNextSessionType()
                _currentSessionType.value = nextSessionType

                // Reset timer state
                _timerState.value = TimerState.COMPLETED
                _progress.value = 100

                // Auto-start next session if enabled
                val currentSettings = _settings.value ?: return@launch
                if (shouldAutoStartNext(nextSessionType, currentSettings)) {
                    // Small delay before auto-starting
                    kotlinx.coroutines.delay(2000)
                    startTimer()
                } else {
                    // Reset for manual start
                    resetTimer()
                }

                Log.d(TAG, "Session completed: ${completedSession.sessionType.displayName}")

            } catch (e: Exception) {
                Log.e(TAG, "Error completing session", e)
                _error.value = "Failed to complete session: ${e.message}"
            }
        }
    }

    private fun updateStatsAfterCompletion(session: PomodoroSession) {
        val currentStats = _stats.value ?: PomodoroStats()
        val newStats = when (session.sessionType) {
            PomodoroSessionType.WORK -> {
                currentStats.copy(
                    completedSessions = currentStats.completedSessions + 1,
                    totalFocusTime = currentStats.totalFocusTime + session.duration,
                    sessionsInCurrentCycle = currentStats.sessionsInCurrentCycle + 1,
                    todaysSessions = currentStats.todaysSessions + session
                )
            }
            else -> {
                currentStats.copy(
                    todaysSessions = currentStats.todaysSessions + session
                )
            }
        }
        _stats.value = newStats
    }

    private fun determineNextSessionType(): PomodoroSessionType {
        val currentStats = _stats.value ?: return PomodoroSessionType.WORK
        val currentSettings = _settings.value ?: return PomodoroSessionType.WORK
        val currentType = _currentSessionType.value ?: return PomodoroSessionType.WORK

        return when (currentType) {
            PomodoroSessionType.WORK -> {
                if (currentStats.sessionsInCurrentCycle >= currentSettings.sessionsUntilLongBreak) {
                    // Reset cycle and take long break
                    _stats.value = currentStats.copy(
                        currentCycle = currentStats.currentCycle + 1,
                        sessionsInCurrentCycle = 0
                    )
                    PomodoroSessionType.LONG_BREAK
                } else {
                    PomodoroSessionType.SHORT_BREAK
                }
            }
            PomodoroSessionType.SHORT_BREAK, PomodoroSessionType.LONG_BREAK -> {
                PomodoroSessionType.WORK
            }
        }
    }

    private fun shouldAutoStartNext(nextType: PomodoroSessionType, settings: PomodoroSettings): Boolean {
        return when (nextType) {
            PomodoroSessionType.WORK -> settings.autoStartWork
            PomodoroSessionType.SHORT_BREAK, PomodoroSessionType.LONG_BREAK -> settings.autoStartBreaks
        }
    }

    fun updateSettings(newSettings: PomodoroSettings) {
        _settings.value = newSettings
        
        // Update current timer duration if stopped
        if (_timerState.value == TimerState.STOPPED) {
            val sessionType = _currentSessionType.value ?: PomodoroSessionType.WORK
            val duration = when (sessionType) {
                PomodoroSessionType.WORK -> newSettings.workDuration
                PomodoroSessionType.SHORT_BREAK -> newSettings.shortBreakDuration
                PomodoroSessionType.LONG_BREAK -> newSettings.longBreakDuration
            }
            _timeRemaining.value = duration
        }
        
        Log.d(TAG, "Settings updated")
    }

    fun linkTodo(todo: ToDo) {
        _linkedTodo.value = todo
        Log.d(TAG, "Linked todo: ${todo.title}")
    }

    fun unlinkTodo() {
        _linkedTodo.value = null
        Log.d(TAG, "Todo unlinked")
    }

    fun switchSessionType(sessionType: PomodoroSessionType) {
        if (_timerState.value == TimerState.RUNNING || _timerState.value == TimerState.PAUSED) {
            // Can't switch while timer is active
            _error.value = "Cannot switch session type while timer is active"
            return
        }

        _currentSessionType.value = sessionType
        val currentSettings = _settings.value ?: return
        val duration = when (sessionType) {
            PomodoroSessionType.WORK -> currentSettings.workDuration
            PomodoroSessionType.SHORT_BREAK -> currentSettings.shortBreakDuration
            PomodoroSessionType.LONG_BREAK -> currentSettings.longBreakDuration
        }
        _timeRemaining.value = duration
        _progress.value = 0

        Log.d(TAG, "Switched to ${sessionType.displayName}")
    }

    private fun loadTodaysStats() {
        viewModelScope.launch {
            try {
                // In a real implementation, you would load today's sessions from database
                // For now, we'll keep stats in memory
                val todayString = dateFormatter.format(Date())
                Log.d(TAG, "Loading stats for today: $todayString")
                
                // Initialize with empty stats for today
                _stats.value = PomodoroStats()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading today's stats", e)
                _error.value = "Failed to load stats: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    class PomodoroViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
                val repository = AppwriteRepository(context)
                val sessionManager = SessionManager(context.applicationContext)
                @Suppress("UNCHECKED_CAST")
                return PomodoroViewModel(repository, sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

enum class TimerState {
    STOPPED,
    RUNNING,
    PAUSED,
    COMPLETED
}
