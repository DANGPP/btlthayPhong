package com.example.noteapp.repository

import android.util.Log
import com.example.noteapp.appwrite.AppwriteConfig
import com.example.noteapp.model.*
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class StatisticsRepository {
    private val databases = AppwriteConfig.databases
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    suspend fun getTodoStatistics(userId: String): TodoStatistics = withContext(Dispatchers.IO) {
        try {
            // Get all todos for user
            val allTodosResponse = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                queries = listOf(Query.equal("userId", userId))
            )
            
            val todos = allTodosResponse.documents.map { doc ->
                ToDo(
                    id = doc.id,
                    title = doc.data["title"] as? String ?: "",
                    description = doc.data["description"] as? String ?: "",
                    createdTime = doc.data["createdTime"] as? String ?: "",
                    dueTime = doc.data["dueTime"] as? String,
                    completedDate = doc.data["completedDate"] as? String,
                    userId = doc.data["userId"] as? String ?: "",
                    status = com.example.noteapp.model.TodoStatus.fromValue(doc.data["status"] as? String ?: "todo"),
                    priority = com.example.noteapp.model.TodoPriority.fromValue(doc.data["priority"] as? String ?: "medium"),
                    category = doc.data["category"] as? String ?: "general"
                )
            }
            
            val totalTodos = todos.size
            val completedTodos = todos.count { it.isCompleted }
            val pendingTodos = totalTodos - completedTodos
            val completionRate = if (totalTodos > 0) (completedTodos.toFloat() / totalTodos) * 100 else 0f
            
            // Calculate daily statistics
            val today = dateFormat.format(Date())
            val todaysCompletedTodos = todos.count { 
                it.isCompleted && it.createdTime.startsWith(today)
            }
            
            // Calculate weekly and monthly stats
            val calendar = Calendar.getInstance()
            val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
            val monthAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
            
            val weeklyCompleted = todos.count { todo ->
                todo.isCompleted && try {
                    val todoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(todo.createdTime)
                    todoDate?.after(weekAgo.time) == true
                } catch (e: Exception) { false }
            }
            
            val monthlyCompleted = todos.count { todo ->
                todo.isCompleted && try {
                    val todoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(todo.createdTime)
                    todoDate?.after(monthAgo.time) == true
                } catch (e: Exception) { false }
            }
            
            // Calculate daily completions for chart
            val dailyCompletions = mutableMapOf<String, Int>()
            for (i in 0..6) {
                val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                val dateStr = dateFormat.format(date.time)
                dailyCompletions[dateStr] = todos.count { todo ->
                    todo.isCompleted && todo.createdTime.startsWith(dateStr)
                }
            }
            
            // Category breakdown
            val categoryBreakdown = todos.filter { it.isCompleted }
                .groupBy { it.category }
                .mapValues { it.value.size }
            
            TodoStatistics(
                totalTodos = totalTodos,
                completedTodos = completedTodos,
                pendingTodos = pendingTodos,
                completionRate = completionRate,
                todaysCompletedTodos = todaysCompletedTodos,
                weeklyCompletedTodos = weeklyCompleted,
                monthlyCompletedTodos = monthlyCompleted,
                averageDailyCompletion = if (weeklyCompleted > 0) weeklyCompleted / 7f else 0f,
                dailyCompletions = dailyCompletions,
                categoryBreakdown = categoryBreakdown
            )
            
        } catch (e: AppwriteException) {
            Log.e("StatisticsRepository", "Error fetching todo statistics: ${e.message}")
            TodoStatistics()
        } catch (e: Exception) {
            Log.e("StatisticsRepository", "Unexpected error: ${e.message}")
            TodoStatistics()
        }
    }
    
    suspend fun getPomodoroStatistics(userId: String): PomodoroStats = withContext(Dispatchers.IO) {
        try {
            // This would fetch from Pomodoro collection if implemented
            // For now, return default stats
            PomodoroStats()
        } catch (e: Exception) {
            Log.e("StatisticsRepository", "Error fetching pomodoro statistics: ${e.message}")
            PomodoroStats()
        }
    }
    
    suspend fun getOverallStatistics(userId: String): OverallStatistics = withContext(Dispatchers.IO) {
        val todoStats = getTodoStatistics(userId)
        val pomodoroStats = getPomodoroStatistics(userId)
        
        val totalProductiveTime = pomodoroStats.totalFocusTime
        val focusScore = calculateFocusScore(todoStats, pomodoroStats)
        
        OverallStatistics(
            todoStats = todoStats,
            pomodoroStats = pomodoroStats,
            totalProductiveTime = totalProductiveTime,
            focusScore = focusScore,
            weeklyGoalProgress = calculateWeeklyProgress(todoStats),
            monthlyGoalProgress = calculateMonthlyProgress(todoStats)
        )
    }
    
    suspend fun getWeeklyReport(userId: String): WeeklyReport = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val weekEnd = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val weekStart = calendar.time
        
        val dailyProductivityList = mutableListOf<DailyProductivity>()
        
        for (i in 0..6) {
            val date = Calendar.getInstance().apply { 
                time = weekStart
                add(Calendar.DAY_OF_YEAR, i) 
            }.time
            
            // This would calculate actual daily productivity
            val dailyProductivity = DailyProductivity(
                date = date,
                completedTodos = 0, // Calculate from actual data
                pomodoroSessions = 0, // Calculate from actual data
                focusTime = 0L,
                productivityScore = 0f
            )
            dailyProductivityList.add(dailyProductivity)
        }
        
        WeeklyReport(
            weekStart = weekStart,
            weekEnd = weekEnd,
            dailyProductivity = dailyProductivityList,
            totalCompletedTodos = dailyProductivityList.sumOf { it.completedTodos },
            totalPomodoroSessions = dailyProductivityList.sumOf { it.pomodoroSessions },
            totalFocusTime = dailyProductivityList.sumOf { it.focusTime },
            averageProductivityScore = dailyProductivityList.map { it.productivityScore }.average().toFloat(),
            bestDay = dailyProductivityList.maxByOrNull { it.productivityScore },
            improvementSuggestions = generateImprovementSuggestions(dailyProductivityList)
        )
    }
    
    private fun calculateFocusScore(todoStats: TodoStatistics, pomodoroStats: PomodoroStats): Float {
        val todoScore = todoStats.completionRate * 0.6f
        val pomodoroScore = if (pomodoroStats.completedSessions > 0) 40f else 0f
        return (todoScore + pomodoroScore).coerceAtMost(100f)
    }
    
    private fun calculateWeeklyProgress(todoStats: TodoStatistics): Float {
        val weeklyGoal = 20 // Default weekly goal
        return ((todoStats.weeklyCompletedTodos.toFloat() / weeklyGoal) * 100).coerceAtMost(100f)
    }
    
    private fun calculateMonthlyProgress(todoStats: TodoStatistics): Float {
        val monthlyGoal = 80 // Default monthly goal
        return ((todoStats.monthlyCompletedTodos.toFloat() / monthlyGoal) * 100).coerceAtMost(100f)
    }
    
    private fun generateImprovementSuggestions(dailyProductivity: List<DailyProductivity>): List<String> {
        val suggestions = mutableListOf<String>()
        
        val avgScore = dailyProductivity.map { it.productivityScore }.average()
        if (avgScore < 50) {
            suggestions.add("Try breaking large tasks into smaller, manageable chunks")
            suggestions.add("Consider using the Pomodoro technique for better focus")
        }
        
        val lowProductivityDays = dailyProductivity.count { it.productivityScore < 30 }
        if (lowProductivityDays > 2) {
            suggestions.add("Establish a consistent daily routine")
            suggestions.add("Set realistic daily goals to maintain motivation")
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("Great work! Keep maintaining your productivity momentum")
        }
        
        return suggestions
    }
}
