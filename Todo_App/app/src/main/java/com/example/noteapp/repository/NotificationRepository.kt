package com.example.noteapp.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.noteapp.model.*
import com.example.noteapp.service.NotificationService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class NotificationRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val notificationService = NotificationService(context)
    
    fun getNotificationSettings(): NotificationSettings {
        val settingsJson = sharedPreferences.getString("notification_settings", null)
        return if (settingsJson != null) {
            gson.fromJson(settingsJson, NotificationSettings::class.java)
        } else {
            NotificationSettings() // Return default settings
        }
    }
    
    fun saveNotificationSettings(settings: NotificationSettings) {
        val settingsJson = gson.toJson(settings)
        sharedPreferences.edit()
            .putString("notification_settings", settingsJson)
            .apply()
        
        // Update scheduled notifications based on new settings
        updateScheduledNotifications(settings)
    }
    
    fun scheduleNotificationsForTodo(todo: ToDo) {
        val settings = getNotificationSettings()
        
        // Schedule reminder if enabled and due date exists
        if (settings.enableReminders && !todo.dueTime.isNullOrEmpty()) {
            try {
                val dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(todo.dueTime)
                if (dueDate != null) {
                    val reminderTime = Calendar.getInstance().apply {
                        time = dueDate
                        add(Calendar.MINUTE, -settings.reminderMinutesBefore)
                    }.time
                    
                    if (reminderTime.after(Date())) {
                        notificationService.scheduleReminderNotification(todo, reminderTime)
                    }
                }
            } catch (e: Exception) {
                // Handle date parsing error
            }
        }
        
        // Schedule deadline notification if enabled
        if (settings.enableDeadlineAlerts && !todo.dueTime.isNullOrEmpty()) {
            try {
                val dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(todo.dueTime)
                if (dueDate != null) {
                    val deadlineTime = Calendar.getInstance().apply {
                        time = dueDate
                        add(Calendar.HOUR_OF_DAY, -settings.deadlineHoursBefore)
                    }.time
                    
                    if (deadlineTime.after(Date())) {
                        notificationService.scheduleDeadlineNotification(todo, deadlineTime)
                    }
                }
            } catch (e: Exception) {
                // Handle date parsing error
            }
        }
    }
    
    fun cancelNotificationsForTodo(todoId: String) {
        notificationService.cancelNotification(todoId)
    }
    
    fun getScheduledNotifications(): List<TodoNotification> {
        val notificationsJson = sharedPreferences.getString("scheduled_notifications", "[]")
        val type = object : TypeToken<List<TodoNotification>>() {}.type
        return gson.fromJson(notificationsJson, type) ?: emptyList()
    }
    
    fun saveScheduledNotification(notification: TodoNotification) {
        val currentNotifications = getScheduledNotifications().toMutableList()
        currentNotifications.add(notification)
        
        val notificationsJson = gson.toJson(currentNotifications)
        sharedPreferences.edit()
            .putString("scheduled_notifications", notificationsJson)
            .apply()
    }
    
    fun removeScheduledNotification(notificationId: String) {
        val currentNotifications = getScheduledNotifications().toMutableList()
        currentNotifications.removeAll { it.id == notificationId }
        
        val notificationsJson = gson.toJson(currentNotifications)
        sharedPreferences.edit()
            .putString("scheduled_notifications", notificationsJson)
            .apply()
    }
    
    private fun updateScheduledNotifications(settings: NotificationSettings) {
        // Update daily summary
        if (settings.enableDailySummary) {
            notificationService.scheduleDailySummary(settings)
        }
        
        // Update weekly summary
        if (settings.enableWeeklySummary) {
            notificationService.scheduleWeeklySummary(settings)
        }
    }
    
    fun checkAndScheduleOverdueNotifications(todos: List<ToDo>) {
        val settings = getNotificationSettings()
        if (!settings.enableOverdueAlerts) return
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Date()
        
        todos.filter { !it.isCompleted && !it.dueTime.isNullOrEmpty() }
            .forEach { todo ->
                try {
                    val dueDate = dateFormat.parse(todo.dueTime)
                    if (dueDate != null && dueDate.before(today)) {
                        // Todo is overdue, show notification
                        notificationService.showNotification(
                            NotificationService.NOTIFICATION_ID_OVERDUE,
                            "Overdue Task: ${todo.title}",
                            "This task was due on ${todo.dueTime}",
                            NotificationService.CHANNEL_ID_DEADLINES
                        )
                    }
                } catch (e: Exception) {
                    // Handle date parsing error
                }
            }
    }
}
