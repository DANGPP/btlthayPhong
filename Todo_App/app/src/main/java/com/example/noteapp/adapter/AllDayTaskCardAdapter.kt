package com.example.noteapp.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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

class AllDayTaskCardAdapter(
    private val context: Context,
    private val onTaskClick: (ToDo) -> Unit
) : ListAdapter<ToDo, AllDayTaskCardAdapter.TaskCardViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_allday_task_card, parent, false)
        return TaskCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTaskId: TextView = itemView.findViewById(R.id.txt_task_id)
        private val txtCategory: TextView = itemView.findViewById(R.id.txt_category)
        private val iconStar: ImageView = itemView.findViewById(R.id.icon_star)
        private val txtTodoTitle: TextView = itemView.findViewById(R.id.txt_todo_title)
        private val txtPriority: TextView = itemView.findViewById(R.id.txt_priority)
        private val txtDuration: TextView = itemView.findViewById(R.id.txt_duration)
        private val txtStatus: TextView = itemView.findViewById(R.id.txt_status)
        private val txtDateTime: TextView = itemView.findViewById(R.id.txt_date_time)
        private val layoutAvatars: LinearLayout = itemView.findViewById(R.id.layout_avatars)
        private val avatar1: ImageView = itemView.findViewById(R.id.avatar1)
        private val avatar2: ImageView = itemView.findViewById(R.id.avatar2)
        private val txtMoreCount: TextView = itemView.findViewById(R.id.txt_more_count)
        private val iconAttachment: ImageView = itemView.findViewById(R.id.icon_attachment)

        fun bind(todo: ToDo) {
            // Generate task ID from actual ID or position
            val taskNumber = if (todo.id.isNotEmpty()) {
                // Extract number from ID or use hash
                todo.id.hashCode() % 1000
            } else {
                adapterPosition + 1
            }
            txtTaskId.text = "Task #$taskNumber"

            // Set category
            txtCategory.text = todo.category
            setCategoryColors(todo.category)

            // Set priority star
            iconStar.visibility = if (todo.priority == TodoPriority.HIGH) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Set title
            txtTodoTitle.text = todo.title

            // Set priority
            txtPriority.text = todo.priority.displayName.uppercase()
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
            txtDuration.text = durationText

            // Set status
            txtStatus.text = todo.status.displayName.uppercase()
            setStatusStyle(todo.status)

            // Set date/time
            val dateTime = formatDateTime(todo.dueTime ?: todo.createdTime)
            txtDateTime.text = dateTime

            // Set avatars (mockup data - in real app, this would come from assignees)
            setupAvatars(todo)

            // Set attachment icon (based on description length as proxy)
            iconAttachment.visibility = if (todo.description.isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Set click listener
            itemView.setOnClickListener { onTaskClick(todo) }
        }

        private fun setCategoryColors(category: String) {
            val (bgColor, textColor) = getCategoryColors(category)
            txtCategory.backgroundTintList = ContextCompat.getColorStateList(context, bgColor)
            txtCategory.setTextColor(ContextCompat.getColor(context, textColor))
        }

        private fun getCategoryColors(category: String): Pair<Int, Int> {
            return when (category.lowercase()) {
                "workout", "fitness", "health" -> Pair(R.color.category_background, R.color.category_text_color)
                "work", "business" -> Pair(R.color.priority_medium, android.R.color.white)
                "personal" -> Pair(R.color.random3, R.color.random1)
                "shopping" -> Pair(R.color.error_background, R.color.error_text_color)
                else -> Pair(R.color.light_gray, R.color.text_secondary)
            }
        }

        private fun setPriorityStyle(priority: TodoPriority) {
            val (bgColor, textColor) = when (priority) {
                TodoPriority.HIGH -> Pair("#FFF3E0", "#F57C00") // Orange
                TodoPriority.MEDIUM -> Pair("#E8F5E8", "#388E3C") // Green
                TodoPriority.LOW -> Pair("#F3E5F5", "#7B1FA2") // Purple
            }
            txtPriority.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(bgColor))
            txtPriority.setTextColor(android.graphics.Color.parseColor(textColor))
        }

        private fun setStatusStyle(status: TodoStatus) {
            val (bgColor, textColor) = when (status) {
                TodoStatus.TODO -> Pair("#E3F2FD", "#1976D2") // Blue
                TodoStatus.IN_PROGRESS -> Pair("#FFF3E0", "#F57C00") // Orange
                TodoStatus.IN_REVIEW -> Pair("#F3E5F5", "#7B1FA2") // Purple
                TodoStatus.COMPLETED -> Pair("#E8F5E8", "#388E3C") // Green
                TodoStatus.DONE -> Pair("#E8F5E8", "#388E3C") // Green (same as completed)
                TodoStatus.ON_HOLD -> Pair("#F5F5F5", "#757575") // Grey
                TodoStatus.CANCELLED -> Pair("#FFEBEE", "#D32F2F") // Red
            }
            txtStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(bgColor))
            txtStatus.setTextColor(android.graphics.Color.parseColor(textColor))
        }

        private fun setupAvatars(todo: ToDo) {
            // Mock avatar setup - in real app, this would be based on task assignees
            // For now, show avatars based on category or some other logic
            val showAvatars = todo.category != "Personal" && todo.priority != TodoPriority.LOW
            
            if (showAvatars) {
                layoutAvatars.visibility = View.VISIBLE
                avatar1.visibility = View.VISIBLE
                avatar2.visibility = View.VISIBLE
                txtMoreCount.visibility = View.VISIBLE
                
                // Set different avatar icons based on category
                when (todo.category.lowercase()) {
                    "work" -> {
                        avatar1.setImageResource(R.drawable.ic_profile)
                        avatar2.setImageResource(R.drawable.ic_profile)
                    }
                    "workout", "fitness" -> {
                        avatar1.setImageResource(R.drawable.ic_profile)
                        avatar2.setImageResource(R.drawable.ic_profile)
                    }
                    else -> {
                        avatar1.setImageResource(R.drawable.ic_profile)
                        avatar2.setImageResource(R.drawable.ic_profile)
                    }
                }
                txtMoreCount.text = "+2"
            } else {
                layoutAvatars.visibility = View.VISIBLE // Always show for demo
                avatar1.visibility = View.VISIBLE
                avatar2.visibility = View.VISIBLE
                txtMoreCount.visibility = View.VISIBLE
                txtMoreCount.text = "+2"
            }
        }

        private fun formatDateTime(dateTimeString: String): String {
            return try {
                // Handle different date formats
                when {
                    dateTimeString.contains("/") && dateTimeString.contains(" ") -> {
                        // Format: "15/10/2022 08:00 AM" or "15/10/2022 14:00 PM"
                        val parts = dateTimeString.split(" ")
                        if (parts.size >= 2) {
                            val datePart = parts[0].split("/")
                            var timePart = parts[1]
                            var ampm = parts.getOrElse(2) { "" }
                            
                            // Fix incorrect time format like "14:00 PM"
                            if (timePart.contains(":")) {
                                val timeComponents = timePart.split(":")
                                var hour = timeComponents[0].toIntOrNull() ?: 0
                                val minute = timeComponents.getOrElse(1) { "00" }
                                
                                // Convert 24-hour format to 12-hour format
                                when {
                                    hour == 0 -> {
                                        hour = 12
                                        ampm = "AM"
                                    }
                                    hour < 12 -> {
                                        ampm = "AM"
                                    }
                                    hour == 12 -> {
                                        ampm = "PM"
                                    }
                                    hour > 12 -> {
                                        hour -= 12
                                        ampm = "PM"
                                    }
                                }
                                
                                timePart = String.format("%02d:%s", hour, minute)
                            }
                            
                            if (datePart.size >= 3) {
                                val day = datePart[0]
                                val month = datePart[1].toIntOrNull() ?: 1
                                val year = datePart[2]
                                return "$day ${getMonthAbbr(month)} $year $timePart $ampm"
                            }
                        }
                        dateTimeString
                    }
                    dateTimeString.contains("-") -> {
                        // ISO format
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val date = sdf.parse(dateTimeString)
                        val outputSdf = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
                        date?.let { outputSdf.format(it).uppercase() } ?: dateTimeString
                    }
                    else -> dateTimeString
                }
            } catch (e: Exception) {
                dateTimeString
            }
        }

        private fun getMonthAbbr(month: Int): String {
            return when (month) {
                1 -> "JAN"; 2 -> "FEB"; 3 -> "MAR"; 4 -> "APR"
                5 -> "MAY"; 6 -> "JUN"; 7 -> "JUL"; 8 -> "AUG"
                9 -> "SEP"; 10 -> "OCT"; 11 -> "NOV"; 12 -> "DEC"
                else -> "JAN"
            }
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