package com.example.noteapp.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.ToDo
import com.example.noteapp.model.TodoPriority
import com.example.noteapp.model.TodoStatus
import java.text.SimpleDateFormat
import java.util.*

class DayTaskAdapter(
    private val context: Context,
    private val onTaskClick: (ToDo) -> Unit,
    private val onStatusToggle: (ToDo) -> Unit
) : ListAdapter<ToDo, DayTaskAdapter.DayTaskViewHolder>(TaskDiffCallback()) {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Submit today's tasks specifically, filtering from the provided list
     */
    fun submitTodaysTasks(allTodos: List<ToDo>) {
        val todayString = dateFormatter.format(Calendar.getInstance().time)
        val todaysTasks = allTodos.filter { todo ->
            val createdDate = extractDateFromDateTime(todo.createdTime)
            val dueDate = extractDateFromDateTime(todo.dueTime)
            val reminderDate = extractDateFromDateTime(todo.reminderTime)
            
            createdDate == todayString || dueDate == todayString || reminderDate == todayString
        }
        submitList(todaysTasks)
    }

    private fun extractDateFromDateTime(dateTimeString: String?): String? {
        if (dateTimeString == null) return null
        return try {
            if (dateTimeString.matches(Regex("\\d{2}/\\d{2}/\\d{4}$"))) {
                dateTimeString
            } else {
                dateTimeString.split(" ")[0]
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayTaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_task, parent, false)
        return DayTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayTaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DayTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val statusIndicator: View = itemView.findViewById(R.id.status_indicator)
        private val txtTaskTitle: TextView = itemView.findViewById(R.id.txt_task_title)
        private val txtTaskTime: TextView = itemView.findViewById(R.id.txt_task_time)
        private val txtTaskCategory: TextView = itemView.findViewById(R.id.txt_task_category)
        private val txtTaskPriority: TextView = itemView.findViewById(R.id.txt_task_priority)
        private val txtTaskDuration: TextView = itemView.findViewById(R.id.txt_task_duration)
        private val txtTaskDescription: TextView = itemView.findViewById(R.id.txt_task_description)
        private val btnTaskStatus: ImageView = itemView.findViewById(R.id.btn_task_status)

        fun bind(todo: ToDo) {
            // Set title
            txtTaskTitle.text = todo.title

            // Set time
            val timeText = extractTimeFromDateTime(todo.dueTime ?: todo.createdTime)
            txtTaskTime.text = timeText

            // Set category
            txtTaskCategory.text = todo.category
            setCategoryStyle(todo.category)

            // Set priority
            txtTaskPriority.text = todo.priority.displayName.uppercase()
            setPriorityStyle(todo.priority)

            // Set duration
            val durationText = if (todo.estimatedDuration > 0) {
                if (todo.estimatedDuration >= 60) {
                    val hours = todo.estimatedDuration / 60
                    val minutes = todo.estimatedDuration % 60
                    if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
                } else {
                    "${todo.estimatedDuration}m"
                }
            } else {
                "N/A"
            }
            txtTaskDuration.text = durationText

            // Set description
            if (todo.description.isNotEmpty()) {
                txtTaskDescription.text = todo.description
                txtTaskDescription.visibility = View.VISIBLE
            } else {
                txtTaskDescription.visibility = View.GONE
            }

            // Set status indicator and button
            setStatusIndicator(todo.status)

            // Set click listeners
            itemView.setOnClickListener { onTaskClick(todo) }
            btnTaskStatus.setOnClickListener { onStatusToggle(todo) }
        }

        private fun extractTimeFromDateTime(dateTimeString: String): String {
            return try {
                when {
                    dateTimeString.contains("/") && dateTimeString.contains(" ") -> {
                        val parts = dateTimeString.split(" ")
                        if (parts.size >= 2) {
                            var timePart = parts[1]
                            val ampm = parts.getOrElse(2) { "" }
                            
                            // Handle 24-hour format conversion
                            if (timePart.contains(":")) {
                                val timeComponents = timePart.split(":")
                                var hour = timeComponents[0].toIntOrNull() ?: 0
                                val minute = timeComponents.getOrElse(1) { "00" }
                                
                                val finalAmPm = when {
                                    hour == 0 -> { hour = 12; "AM" }
                                    hour < 12 -> "AM"
                                    hour == 12 -> "PM"
                                    hour > 12 -> { hour -= 12; "PM" }
                                    else -> ampm
                                }
                                
                                timePart = String.format("%02d:%s", hour, minute)
                                "$timePart $finalAmPm"
                            } else {
                                "$timePart $ampm"
                            }
                        } else {
                            dateTimeString
                        }
                    }
                    dateTimeString.contains("-") -> {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val date = sdf.parse(dateTimeString)
                        val outputSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        date?.let { outputSdf.format(it).uppercase() } ?: dateTimeString
                    }
                    else -> dateTimeString
                }
            } catch (e: Exception) {
                dateTimeString
            }
        }

        private fun setCategoryStyle(category: String) {
            val (bgColor, textColor) = when (category.lowercase()) {
                "workout", "fitness", "health" -> Pair("#E3F2FD", "#1976D2")
                "work", "business" -> Pair("#FFF3E0", "#F57C00")
                "personal" -> Pair("#F3E5F5", "#7B1FA2")
                "shopping" -> Pair("#FFEBEE", "#D32F2F")
                else -> Pair("#F5F5F5", "#757575")
            }
            txtTaskCategory.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(bgColor))
            txtTaskCategory.setTextColor(Color.parseColor(textColor))
        }

        private fun setPriorityStyle(priority: TodoPriority) {
            val (bgColor, textColor) = when (priority) {
                TodoPriority.URGENT -> Pair("#FFEBEE", "#D32F2F")
                TodoPriority.HIGH -> Pair("#FFF3E0", "#F57C00")
                TodoPriority.MEDIUM -> Pair("#E8F5E8", "#388E3C")
                TodoPriority.LOW -> Pair("#F3E5F5", "#7B1FA2")
            }
            txtTaskPriority.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(bgColor))
            txtTaskPriority.setTextColor(Color.parseColor(textColor))
        }

        private fun setStatusIndicator(status: TodoStatus) {
            val (indicatorColor, iconRes, iconTint) = when (status) {
                TodoStatus.TODO -> Triple("#2196F3", R.drawable.ic_schedule, "#999999")
                TodoStatus.IN_PROGRESS -> Triple("#FF9800", R.drawable.ic_play_arrow, "#FF9800")
                TodoStatus.COMPLETED -> Triple("#4CAF50", R.drawable.ic_check_circle, "#4CAF50")
                TodoStatus.ON_HOLD -> Triple("#9E9E9E", R.drawable.ic_pause, "#9E9E9E")
                TodoStatus.CANCELLED -> Triple("#607D8B", R.drawable.baseline_radio_button_unchecked_24, "#607D8B")
            }
            
            statusIndicator.setBackgroundColor(Color.parseColor(indicatorColor))
            btnTaskStatus.setImageResource(iconRes)
            btnTaskStatus.setColorFilter(Color.parseColor(iconTint))
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<ToDo>() {
        override fun areItemsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem == newItem
        }
    }
}