package com.example.noteapp.model

import java.util.*

data class TodoNotification(
    val id: String = UUID.randomUUID().toString(),
    val todoId: String,
    val userId: String,
    val title: String,
    val message: String,
    val scheduledTime: Date,
    val notificationType: NotificationType,
    val isScheduled: Boolean = false,
    val isDelivered: Boolean = false,
    val createdAt: Date = Date()
)

enum class NotificationType(val displayName: String) {
    REMINDER("Reminder"),
    DEADLINE("Deadline"),
    OVERDUE("Overdue"),
    DAILY_SUMMARY("Daily Summary"),
    WEEKLY_SUMMARY("Weekly Summary")
}

data class NotificationSettings(
    val enableReminders: Boolean = true,
    val enableDeadlineAlerts: Boolean = true,
    val enableOverdueAlerts: Boolean = true,
    val enableDailySummary: Boolean = true,
    val enableWeeklySummary: Boolean = false,
    val reminderMinutesBefore: Int = 60, // Default 1 hour before
    val deadlineHoursBefore: Int = 24, // Default 24 hours before
    val dailySummaryTime: String = "18:00", // 6 PM
    val weeklySummaryDay: Int = Calendar.SUNDAY,
    val weeklySummaryTime: String = "19:00", // 7 PM
    val quietHoursStart: String = "22:00", // 10 PM
    val quietHoursEnd: String = "08:00" // 8 AM
)
