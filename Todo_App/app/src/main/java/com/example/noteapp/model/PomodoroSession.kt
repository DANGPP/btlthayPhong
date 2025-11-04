package com.example.noteapp.model

import java.util.*

data class PomodoroSession(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val sessionType: PomodoroSessionType,
    val duration: Long, // in milliseconds
    val startTime: Date,
    val endTime: Date? = null,
    val isCompleted: Boolean = false,
    val linkedTodoId: String? = null
)

enum class PomodoroSessionType(val displayName: String) {
    WORK("Work Session"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break")
}

data class PomodoroSettings(
    val workDuration: Long = 25 * 60 * 1000L, // 25 minutes in milliseconds
    val shortBreakDuration: Long = 5 * 60 * 1000L, // 5 minutes in milliseconds
    val longBreakDuration: Long = 15 * 60 * 1000L, // 15 minutes in milliseconds
    val sessionsUntilLongBreak: Int = 4,
    val autoStartBreaks: Boolean = false,
    val autoStartWork: Boolean = false
)

data class PomodoroStats(
    val completedSessions: Int = 0,
    val totalFocusTime: Long = 0L, // in milliseconds
    val currentCycle: Int = 1,
    val sessionsInCurrentCycle: Int = 0,
    val todaysSessions: List<PomodoroSession> = emptyList()
)
