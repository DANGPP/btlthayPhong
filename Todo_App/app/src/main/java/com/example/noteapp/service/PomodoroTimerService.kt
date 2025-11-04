package com.example.noteapp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.noteapp.MainActivity
import com.example.noteapp.R
import com.example.noteapp.model.PomodoroSessionType
import java.util.concurrent.TimeUnit

class PomodoroTimerService : Service() {

    companion object {
        private const val TAG = "PomodoroTimerService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "pomodoro_timer_channel"
        
        // Broadcast actions
        const val ACTION_TIMER_TICK = "com.example.noteapp.TIMER_TICK"
        const val ACTION_TIMER_FINISHED = "com.example.noteapp.TIMER_FINISHED"
        const val ACTION_TIMER_PAUSED = "com.example.noteapp.TIMER_PAUSED"
        const val ACTION_TIMER_RESUMED = "com.example.noteapp.TIMER_RESUMED"
        
        // Intent extras
        const val EXTRA_TIME_REMAINING = "time_remaining"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_SESSION_TYPE = "session_type"
        
        // Service actions
        const val ACTION_START_TIMER = "start_timer"
        const val ACTION_PAUSE_TIMER = "pause_timer"
        const val ACTION_RESUME_TIMER = "resume_timer"
        const val ACTION_STOP_TIMER = "stop_timer"
    }

    private val binder = PomodoroTimerBinder()
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var isPaused = false
    
    // Timer state
    private var totalDuration: Long = 0
    private var timeRemaining: Long = 0
    private var sessionType: PomodoroSessionType = PomodoroSessionType.WORK
    private var sessionTitle: String = ""

    inner class PomodoroTimerBinder : Binder() {
        fun getService(): PomodoroTimerService = this@PomodoroTimerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "PomodoroTimerService created")
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val duration = intent.getLongExtra("duration", 25 * 60 * 1000L)
                val type = intent.getSerializableExtra("session_type") as? PomodoroSessionType 
                    ?: PomodoroSessionType.WORK
                val title = intent.getStringExtra("title") ?: type.displayName
                startTimer(duration, type, title)
            }
            ACTION_PAUSE_TIMER -> pauseTimer()
            ACTION_RESUME_TIMER -> resumeTimer()
            ACTION_STOP_TIMER -> stopTimer()
        }
        return START_STICKY
    }

    fun startTimer(duration: Long, type: PomodoroSessionType, title: String) {
        if (isTimerRunning) {
            stopTimer()
        }

        totalDuration = duration
        timeRemaining = duration
        sessionType = type
        sessionTitle = title
        isTimerRunning = true
        isPaused = false

        startForeground(NOTIFICATION_ID, createNotification())

        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateNotification()
                broadcastTimerTick()
            }

            override fun onFinish() {
                onTimerFinished()
            }
        }.start()

        Log.d(TAG, "Timer started: $title for ${duration / 1000} seconds")
    }

    fun pauseTimer() {
        if (isTimerRunning && !isPaused) {
            countDownTimer?.cancel()
            isPaused = true
            updateNotification()
            broadcastTimerPaused()
            Log.d(TAG, "Timer paused")
        }
    }

    fun resumeTimer() {
        if (isTimerRunning && isPaused) {
            isPaused = false
            
            countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeRemaining = millisUntilFinished
                    updateNotification()
                    broadcastTimerTick()
                }

                override fun onFinish() {
                    onTimerFinished()
                }
            }.start()

            updateNotification()
            broadcastTimerResumed()
            Log.d(TAG, "Timer resumed")
        }
    }

    fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        isPaused = false
        stopForeground(true)
        stopSelf()
        Log.d(TAG, "Timer stopped")
    }

    private fun onTimerFinished() {
        isTimerRunning = false
        isPaused = false
        
        // Show completion notification
        showCompletionNotification()
        
        // Broadcast completion
        broadcastTimerFinished()
        
        // Stop the service
        stopForeground(true)
        stopSelf()
        
        Log.d(TAG, "Timer finished: $sessionTitle")
    }

    private fun broadcastTimerTick() {
        val intent = Intent(ACTION_TIMER_TICK).apply {
            putExtra(EXTRA_TIME_REMAINING, timeRemaining)
            putExtra(EXTRA_PROGRESS, calculateProgress())
            putExtra(EXTRA_SESSION_TYPE, sessionType)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastTimerFinished() {
        val intent = Intent(ACTION_TIMER_FINISHED).apply {
            putExtra(EXTRA_SESSION_TYPE, sessionType)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastTimerPaused() {
        val intent = Intent(ACTION_TIMER_PAUSED).apply {
            putExtra(EXTRA_TIME_REMAINING, timeRemaining)
            putExtra(EXTRA_SESSION_TYPE, sessionType)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastTimerResumed() {
        val intent = Intent(ACTION_TIMER_RESUMED).apply {
            putExtra(EXTRA_TIME_REMAINING, timeRemaining)
            putExtra(EXTRA_SESSION_TYPE, sessionType)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun calculateProgress(): Int {
        if (totalDuration == 0L) return 0
        val elapsed = totalDuration - timeRemaining
        return ((elapsed.toFloat() / totalDuration.toFloat()) * 100).toInt()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Pomodoro timer notifications"
                setSound(null, null)
                enableVibration(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val statusText = when {
            isPaused -> "Paused"
            isTimerRunning -> "Running"
            else -> "Stopped"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$sessionTitle - $statusText")
            .setContentText(timeText)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(createPauseResumeAction())
            .addAction(createStopAction())
            .build()
    }

    private fun createPauseResumeAction(): NotificationCompat.Action {
        val actionIntent = if (isPaused) {
            Intent(this, PomodoroTimerService::class.java).apply {
                action = ACTION_RESUME_TIMER
            }
        } else {
            Intent(this, PomodoroTimerService::class.java).apply {
                action = ACTION_PAUSE_TIMER
            }
        }

        val pendingIntent = PendingIntent.getService(
            this, 0, actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionText = if (isPaused) "Resume" else "Pause"
        val actionIcon = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause

        return NotificationCompat.Action.Builder(actionIcon, actionText, pendingIntent).build()
    }

    private fun createStopAction(): NotificationCompat.Action {
        val stopIntent = Intent(this, PomodoroTimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_refresh, "Stop", stopPendingIntent
        ).build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun showCompletionNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completionNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$sessionTitle Completed!")
            .setContentText("Time to ${getNextSessionMessage()}")
            .setSmallIcon(R.drawable.ic_check_circle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, completionNotification)
    }

    private fun getNextSessionMessage(): String {
        return when (sessionType) {
            PomodoroSessionType.WORK -> "take a break"
            PomodoroSessionType.SHORT_BREAK -> "get back to work"
            PomodoroSessionType.LONG_BREAK -> "start a new cycle"
        }
    }

    // Public methods for service state
    fun isRunning(): Boolean = isTimerRunning
    fun isPaused(): Boolean = isPaused
    fun getTimeRemaining(): Long = timeRemaining
    fun getSessionType(): PomodoroSessionType = sessionType
    fun getProgress(): Int = calculateProgress()

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        Log.d(TAG, "PomodoroTimerService destroyed")
    }
}
