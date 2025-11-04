package com.example.noteapp.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.TimeSlot
import com.example.noteapp.model.ToDo
import java.text.SimpleDateFormat
import java.util.*

class TimelineScheduleAdapter(
    private val context: Context,
    private val onTaskClick: (ToDo) -> Unit
) : RecyclerView.Adapter<TimelineScheduleAdapter.TimeSlotViewHolder>() {

    private val timeSlots = generateTimeSlots()
    private var tasks: List<ToDo> = emptyList()
    
    // Category colors for different task types
    private val categoryColors = mapOf(
        "Finance" to Pair("#E1F5FE", "#0288D1"),
        "Health" to Pair("#E8F5E9", "#388E3C"),
        "Work" to Pair("#FFF3E0", "#F57C00"),
        "Personal" to Pair("#F3E5F5", "#7B1FA2"),
        "Fitness" to Pair("#E3F2FD", "#1976D2"),
        "Shopping" to Pair("#FFEBEE", "#D32F2F"),
        "Meeting" to Pair("#E0F2F1", "#00796B"),
        "Other" to Pair("#F5F5F5", "#757575")
    )

    class TimeSlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTime: TextView = view.findViewById(R.id.textTime)
        val taskContainer: FrameLayout = view.findViewById(R.id.taskContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline_slot_modern, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        
        // Set time display
        holder.textTime.text = timeSlot.displayTime
        
        // Create dotted line effect for grid line
        val gridLine = holder.itemView.findViewById<View>(R.id.gridLine)
        if (gridLine != null) {
            // Create a simple dotted line using a drawable resource
            val drawable = ContextCompat.getDrawable(context, R.drawable.dotted_line)
            gridLine.background = drawable
        }
        
        // Clear previous tasks
        holder.taskContainer.removeAllViews()
        
        // Add tasks that belong to this time slot
        val tasksForThisSlot = getTasksForTimeSlot(timeSlot)
        
        if (tasksForThisSlot.isNotEmpty()) {
            for (task in tasksForThisSlot) {
                val taskView = createTaskView(task)
                holder.taskContainer.addView(taskView)
            }
        }
    }

    private fun createTaskView(task: ToDo): View {
        val taskView = LayoutInflater.from(context)
            .inflate(R.layout.item_task_card_modern, null, false)
        
        // Set task details
        val textTitle = taskView.findViewById<TextView>(R.id.textTitle)
        val textTime = taskView.findViewById<TextView>(R.id.textTime)
        val textCategory = taskView.findViewById<TextView>(R.id.textCategory)
        val cardView = taskView as CardView
        
        textTitle.text = task.title
        
        // Format time display
        val timeDisplay = formatTimeDisplay(task)
        textTime.text = timeDisplay
        
        // Set category with appropriate color
        val category = getCategoryForTask(task)
        textCategory.text = category
        
        // Set category colors
        val (bgColor, textColor) = categoryColors[category] ?: categoryColors["Other"]!!
        textCategory.setBackgroundColor(Color.parseColor(bgColor))
        textCategory.setTextColor(Color.parseColor(textColor))
        
        // Set click listener
        cardView.setOnClickListener { onTaskClick(task) }
        
        return taskView
    }

    private fun formatTimeDisplay(task: ToDo): String {
        val startTime = extractTimeFromDateTime(task.reminderTime ?: task.createdTime)
        val endTime = extractTimeFromDateTime(task.dueTime ?: "")
        
        return if (endTime.isNotEmpty()) {
            "$startTime - $endTime"
        } else {
            startTime
        }
    }
    
    private fun extractTimeFromDateTime(dateTime: String?): String {
        if (dateTime.isNullOrEmpty()) return ""
        
        return try {
            if (dateTime.contains(" ")) {
                val timePart = dateTime.split(" ")[1]
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val timeObj = formatter.parse(timePart)
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(timeObj!!)
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun getCategoryForTask(task: ToDo): String {
        // In a real app, you would have a category field in your ToDo model
        // For now, we'll assign categories based on title keywords
        return when {
            task.title.contains("pay", true) || 
            task.title.contains("bill", true) || 
            task.title.contains("money", true) -> "Finance"
            
            task.title.contains("meet", true) || 
            task.title.contains("call", true) || 
            task.title.contains("conference", true) -> "Meeting"
            
            task.title.contains("doctor", true) || 
            task.title.contains("medicine", true) || 
            task.title.contains("health", true) -> "Health"
            
            task.title.contains("gym", true) || 
            task.title.contains("workout", true) || 
            task.title.contains("exercise", true) -> "Fitness"
            
            task.title.contains("buy", true) || 
            task.title.contains("shop", true) || 
            task.title.contains("purchase", true) -> "Shopping"
            
            task.title.contains("work", true) || 
            task.title.contains("project", true) || 
            task.title.contains("report", true) -> "Work"
            
            else -> "Personal"
        }
    }

    private fun getTasksForTimeSlot(timeSlot: TimeSlot): List<ToDo> {
        val hour = timeSlot.hour
        
        return tasks.filter { task ->
            val taskHour = extractHourFromTask(task)
            taskHour == hour
        }
    }
    
    private fun extractHourFromTask(task: ToDo): Int {
        val timeString = task.reminderTime ?: task.createdTime
        
        return try {
            if (timeString.contains(" ")) {
                val timePart = timeString.split(" ")[1]
                val hour = timePart.split(":")[0].toInt()
                hour
            } else {
                -1
            }
        } catch (e: Exception) {
            -1
        }
    }

    override fun getItemCount(): Int = timeSlots.size

    fun updateTasks(newTasks: List<ToDo>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    private fun generateTimeSlots(): List<TimeSlot> {
        val slots = mutableListOf<TimeSlot>()
        for (hour in 0..23) {
            val displayTime = when {
                hour == 0 -> "12 AM"
                hour < 12 -> "$hour AM"
                hour == 12 -> "12 PM"
                else -> "${hour - 12} PM"
            }
            slots.add(TimeSlot(hour, displayTime, emptyList()))
        }
        return slots
    }
}
