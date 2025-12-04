package com.example.noteapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.ToDo
import com.example.noteapp.model.TodoStatus
import java.text.SimpleDateFormat
import java.util.*

data class TimelineSlot(
    val hour: Int,
    val displayTime: String,
    val todos: List<ToDo> = emptyList()
)

class DayTimelineAdapter(
    private val context: Context,
    private val onTodoClick: (ToDo) -> Unit
) : RecyclerView.Adapter<DayTimelineAdapter.TimelineViewHolder>() {

    private var timelineSlots = mutableListOf<TimelineSlot>()
    
    init {
        // Generate all 24 hour slots
        generateTimeSlots()
    }

    private fun generateTimeSlots() {
        timelineSlots.clear()
        for (hour in 0..23) {
            val displayTime = formatHourToDisplayTime(hour)
            timelineSlots.add(TimelineSlot(hour, displayTime))
        }
    }

    fun updateWithTodos(todos: List<ToDo>) {
        // Group todos by hour
        val todosByHour = todos.groupBy { todo ->
            extractHourFromTodo(todo)
        }.filterKeys { it >= 0 }

        // Update timeline slots with todos
        timelineSlots.forEachIndexed { index, slot ->
            timelineSlots[index] = slot.copy(todos = todosByHour[slot.hour] ?: emptyList())
        }
        
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_timeline_slot, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.bind(timelineSlots[position])
    }

    override fun getItemCount(): Int = timelineSlots.size

    inner class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val todoContainer: LinearLayout = itemView.findViewById(R.id.todoContainer)

        fun bind(slot: TimelineSlot) {
            timeText.text = slot.displayTime
            
            // Clear previous todos
            todoContainer.removeAllViews()
            
            // Add todos for this time slot
            slot.todos.forEach { todo ->
                val todoView = createTodoView(todo)
                todoContainer.addView(todoView)
            }
            
            // Show empty state if no todos
            if (slot.todos.isEmpty()) {
                todoContainer.visibility = View.GONE
            } else {
                todoContainer.visibility = View.VISIBLE
            }
        }

        private fun createTodoView(todo: ToDo): View {
            val todoView = LayoutInflater.from(context).inflate(R.layout.item_timeline_todo, todoContainer, false)
            
            val titleText: TextView = todoView.findViewById(R.id.todoTitle)
            val timeText: TextView = todoView.findViewById(R.id.todoTime)
            val categoryText: TextView = todoView.findViewById(R.id.todoCategory)
            val statusIndicator: View = todoView.findViewById(R.id.statusIndicator)
            
            titleText.text = todo.title
            
            // Extract and format time
            val timeString = extractTimeFromTodo(todo)
            timeText.text = timeString
            
            // Set category with color
            categoryText.text = todo.category ?: "General"
            val categoryColor = getCategoryColor(todo.category)
            categoryText.setTextColor(categoryColor)
            
            // Set status indicator color
            val statusColor = getStatusColor(todo.status)
            statusIndicator.setBackgroundColor(statusColor)
            
            // Handle click
            todoView.setOnClickListener { onTodoClick(todo) }
            
            return todoView
        }
    }

    private fun extractHourFromTodo(todo: ToDo): Int {
        return try {
            val timeString = when {
                !todo.reminderTime.isNullOrEmpty() -> todo.reminderTime
                !todo.dueTime.isNullOrEmpty() -> todo.dueTime
                else -> todo.createdTime
            }
            
            if (timeString?.contains(" ") == true && timeString.split(" ").size >= 2) {
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

    private fun extractTimeFromTodo(todo: ToDo): String {
        return try {
            val timeString = when {
                !todo.reminderTime.isNullOrEmpty() -> todo.reminderTime
                !todo.dueTime.isNullOrEmpty() -> todo.dueTime
                else -> todo.createdTime
            }
            
            if (timeString?.contains(" ") == true && timeString.split(" ").size >= 2) {
                val timePart = timeString.split(" ")[1]
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val time = sdf.parse(timePart)
                val displaySdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                displaySdf.format(time ?: Date())
            } else {
                "All day"
            }
        } catch (e: Exception) {
            "All day"
        }
    }

    private fun formatHourToDisplayTime(hour: Int): String {
        return when {
            hour == 0 -> "12:00"
            hour < 10 -> "0$hour:00"
            else -> "$hour:00"
        }
    }

    private fun getCategoryColor(category: String?): Int {
        return when (category?.lowercase()) {
            "finance" -> ContextCompat.getColor(context, R.color.category_finance)
            "health" -> ContextCompat.getColor(context, R.color.category_health)
            "fitness" -> ContextCompat.getColor(context, R.color.category_fitness)
            else -> ContextCompat.getColor(context, R.color.category_general)
        }
    }

    private fun getStatusColor(status: TodoStatus?): Int {
        return when (status) {
            TodoStatus.TODO -> ContextCompat.getColor(context, R.color.status_todo)
            TodoStatus.IN_PROGRESS -> ContextCompat.getColor(context, R.color.status_in_progress)
            TodoStatus.IN_REVIEW -> ContextCompat.getColor(context, R.color.status_in_progress)
            TodoStatus.COMPLETED -> ContextCompat.getColor(context, R.color.status_completed)
            TodoStatus.DONE -> ContextCompat.getColor(context, R.color.status_completed)
            else -> ContextCompat.getColor(context, R.color.status_todo)
        }
    }
}
