package com.example.noteapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.noteapp.MainActivity
import com.example.noteapp.R
import com.example.noteapp.model.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationService(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID_REMINDERS = "todo_reminders"
        const val CHANNEL_ID_DEADLINES = "todo_deadlines"
        const val CHANNEL_ID_SUMMARIES = "todo_summaries"
        
        const val NOTIFICATION_ID_REMINDER = 1001
        const val NOTIFICATION_ID_DEADLINE = 1002
        const val NOTIFICATION_ID_OVERDUE = 1003
        const val NOTIFICATION_ID_DAILY_SUMMARY = 1004
        const val NOTIFICATION_ID_WEEKLY_SUMMARY = 1005
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Reminders channel
            val remindersChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Todo Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for todo reminders"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Deadlines channel
            val deadlinesChannel = NotificationChannel(
                CHANNEL_ID_DEADLINES,
                "Todo Deadlines",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for todo deadlines and overdue items"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Summaries channel
            val summariesChannel = NotificationChannel(
                CHANNEL_ID_SUMMARIES,
                "Daily & Weekly Summaries",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily and weekly productivity summaries"
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannels(
                listOf(remindersChannel, deadlinesChannel, summariesChannel)
            )
        }
    }
    
    fun scheduleReminderNotification(todo: ToDo, reminderTime: Date) {
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(
                Data.Builder()
                    .putString("todo_id", todo.id)
                    .putString("todo_title", todo.title)
                    .putString("todo_description", todo.description)
                    .build()
            )
            .setInitialDelay(calculateDelay(reminderTime), TimeUnit.MILLISECONDS)
            .addTag("reminder_${todo.id}")
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    fun scheduleDeadlineNotification(todo: ToDo, deadlineTime: Date) {
        val workRequest = OneTimeWorkRequestBuilder<DeadlineWorker>()
            .setInputData(
                Data.Builder()
                    .putString("todo_id", todo.id)
                    .putString("todo_title", todo.title)
                    .putString("due_date", todo.dueTime ?: "")
                    .build()
            )
            .setInitialDelay(calculateDelay(deadlineTime), TimeUnit.MILLISECONDS)
            .addTag("deadline_${todo.id}")
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    fun scheduleDailySummary(settings: NotificationSettings) {
        val workRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDailySummaryDelay(settings.dailySummaryTime), TimeUnit.MILLISECONDS)
            .addTag("daily_summary")
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_summary",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    fun scheduleWeeklySummary(settings: NotificationSettings) {
        if (!settings.enableWeeklySummary) return
        
        val workRequest = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(calculateWeeklySummaryDelay(settings), TimeUnit.MILLISECONDS)
            .addTag("weekly_summary")
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "weekly_summary",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    fun cancelNotification(todoId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$todoId")
        WorkManager.getInstance(context).cancelAllWorkByTag("deadline_$todoId")
    }
    
    fun showNotification(
        notificationId: Int,
        title: String,
        message: String,
        channelId: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    private fun calculateDelay(targetTime: Date): Long {
        val currentTime = System.currentTimeMillis()
        val targetTimeMillis = targetTime.time
        return maxOf(0L, targetTimeMillis - currentTime)
    }
    
    private fun calculateDailySummaryDelay(timeString: String): Long {
        val calendar = Calendar.getInstance()
        val timeParts = timeString.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        
        // If the time has passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis - System.currentTimeMillis()
    }
    
    private fun calculateWeeklySummaryDelay(settings: NotificationSettings): Long {
        val calendar = Calendar.getInstance()
        val timeParts = settings.weeklySummaryTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        
        calendar.set(Calendar.DAY_OF_WEEK, settings.weeklySummaryDay)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        
        // If the time has passed this week, schedule for next week
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis - System.currentTimeMillis()
    }
}

// Worker classes for different notification types
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val todoId = inputData.getString("todo_id") ?: return Result.failure()
        val todoTitle = inputData.getString("todo_title") ?: "Todo Reminder"
        val todoDescription = inputData.getString("todo_description") ?: ""
        
        val notificationService = NotificationService(applicationContext)
        notificationService.showNotification(
            NotificationService.NOTIFICATION_ID_REMINDER,
            "Todo Reminder: $todoTitle",
            todoDescription.ifEmpty { "Don't forget to complete this task!" },
            NotificationService.CHANNEL_ID_REMINDERS
        )
        
        return Result.success()
    }
}

class DeadlineWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val todoId = inputData.getString("todo_id") ?: return Result.failure()
        val todoTitle = inputData.getString("todo_title") ?: "Todo Deadline"
        val dueTime = inputData.getString("due_date") ?: ""
        
        val notificationService = NotificationService(applicationContext)
        notificationService.showNotification(
            NotificationService.NOTIFICATION_ID_DEADLINE,
            "Deadline Approaching: $todoTitle",
            "Due: $dueTime",
            NotificationService.CHANNEL_ID_DEADLINES,
            NotificationCompat.PRIORITY_HIGH
        )
        
        return Result.success()
    }
}

class DailySummaryWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        // This would fetch actual statistics and create a summary
        val notificationService = NotificationService(applicationContext)
        notificationService.showNotification(
            NotificationService.NOTIFICATION_ID_DAILY_SUMMARY,
            "Daily Summary",
            "Check your productivity stats for today!",
            NotificationService.CHANNEL_ID_SUMMARIES,
            NotificationCompat.PRIORITY_LOW
        )
        
        return Result.success()
    }
}

class WeeklySummaryWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val notificationService = NotificationService(applicationContext)
        notificationService.showNotification(
            NotificationService.NOTIFICATION_ID_WEEKLY_SUMMARY,
            "Weekly Summary",
            "Your weekly productivity report is ready!",
            NotificationService.CHANNEL_ID_SUMMARIES,
            NotificationCompat.PRIORITY_LOW
        )
        
        return Result.success()
    }
}
