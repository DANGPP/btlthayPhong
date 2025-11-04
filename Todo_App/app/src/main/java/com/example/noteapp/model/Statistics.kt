package com.example.noteapp.model

import java.util.*

data class TodoStatistics(
    val totalTodos: Int = 0,
    val completedTodos: Int = 0,
    val pendingTodos: Int = 0,
    val completionRate: Float = 0f,
    val todaysCompletedTodos: Int = 0,
    val weeklyCompletedTodos: Int = 0,
    val monthlyCompletedTodos: Int = 0,
    val averageDailyCompletion: Float = 0f,
    val streakDays: Int = 0,
    val longestStreak: Int = 0,
    val dailyCompletions: Map<String, Int> = emptyMap(), // Date string to completion count
    val categoryBreakdown: Map<String, Int> = emptyMap(), // Category to count
    val productiveHours: Map<Int, Int> = emptyMap() // Hour of day to completion count
)

data class OverallStatistics(
    val todoStats: TodoStatistics,
    val pomodoroStats: PomodoroStats,
    val totalProductiveTime: Long = 0L, // in milliseconds
    val focusScore: Float = 0f, // 0-100 based on completion rate and pomodoro sessions
    val weeklyGoalProgress: Float = 0f, // 0-100 percentage
    val monthlyGoalProgress: Float = 0f // 0-100 percentage
)

data class DailyProductivity(
    val date: Date,
    val completedTodos: Int,
    val pomodoroSessions: Int,
    val focusTime: Long, // in milliseconds
    val productivityScore: Float // 0-100
)

data class WeeklyReport(
    val weekStart: Date,
    val weekEnd: Date,
    val dailyProductivity: List<DailyProductivity>,
    val totalCompletedTodos: Int,
    val totalPomodoroSessions: Int,
    val totalFocusTime: Long,
    val averageProductivityScore: Float,
    val bestDay: DailyProductivity?,
    val improvementSuggestions: List<String>
)
