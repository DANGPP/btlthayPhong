package com.example.noteapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.databinding.ItemCalendarTodoBinding
import com.example.noteapp.model.ToDo
import com.example.noteapp.model.TodoPriority
import com.example.noteapp.model.TodoStatus

class CalendarTodoAdapter(
    private val context: Context,
    private val onTodoClick: (ToDo) -> Unit,
    private val onStatusChange: (ToDo) -> Unit
) : ListAdapter<ToDo, CalendarTodoAdapter.CalendarTodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarTodoViewHolder {
        val binding = ItemCalendarTodoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarTodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarTodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CalendarTodoViewHolder(
        private val binding: ItemCalendarTodoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: ToDo) {
            binding.apply {
                textTitle.text = todo.title
                
                // Set time from created time or use default
                val timeText = extractTimeFromDate(todo.createdTime)
                textTime.text = timeText
                
                // Set time range (for now, use a default range)
                textTimeRange.text = "$timeText - ${getEndTime(timeText)}"
                
                // Set time indicator color based on status
                val indicatorColor = when (todo.status) {
                    TodoStatus.COMPLETED -> R.color.status_completed
                    TodoStatus.IN_PROGRESS -> R.color.status_in_progress
                    TodoStatus.TODO -> R.color.priority_medium
                    else -> R.color.priority_low
                }
                
                timeIndicator.setBackgroundResource(indicatorColor)
                
                // Set click listeners
                root.setOnClickListener { onTodoClick(todo) }
                
                // Set completion visual state
                setCompletionState(todo.status == TodoStatus.COMPLETED)
            }
        }
        
        private fun extractTimeFromDate(dateString: String): String {
            return try {
                // Extract time from date string or use current time
                val calendar = java.util.Calendar.getInstance()
                val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                val amPm = if (hour < 12) "AM" else "PM"
                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                "$displayHour $amPm"
            } catch (e: Exception) {
                "9 AM"
            }
        }
        
        private fun getEndTime(startTime: String): String {
            return try {
                val parts = startTime.split(" ")
                val hour = parts[0].toInt()
                val amPm = parts[1]
                val endHour = hour + 3
                "$endHour$amPm"
            } catch (e: Exception) {
                "12PM"
            }
        }
        
        private fun setCompletionState(isCompleted: Boolean) {
            binding.apply {
                val alpha = if (isCompleted) 0.6f else 1.0f
                textTitle.alpha = alpha
                textTimeRange.alpha = alpha
                
                if (isCompleted) {
                    textTitle.paintFlags = textTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    textTitle.paintFlags = textTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }
        
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<ToDo>() {
        override fun areItemsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem == newItem
        }
    }
}
