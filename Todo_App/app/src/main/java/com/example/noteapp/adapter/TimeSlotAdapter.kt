package com.example.noteapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.databinding.ItemCalendarTodoBinding
import com.example.noteapp.model.TimeSlot
import com.example.noteapp.model.ToDo
import com.example.noteapp.model.TodoStatus

class TimeSlotAdapter(
    private val context: Context,
    private val onTodoClick: (ToDo) -> Unit
) : ListAdapter<TimeSlot, TimeSlotAdapter.TimeSlotViewHolder>(TimeSlotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemCalendarTodoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimeSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TimeSlotViewHolder(
        private val binding: ItemCalendarTodoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timeSlot: TimeSlot) {
            binding.apply {
                if (timeSlot.tasks.isNotEmpty()) {
                    // Show the first task for this time slot
                    val task = timeSlot.tasks.first()
                    
                    // Extract actual time from task data
                    val actualTime = extractActualTimeFromTask(task)
                    textTime.text = actualTime ?: timeSlot.displayTime
                    
                    textTitle.text = task.title
                    
                    // Show actual time range from task data
                    val endTime = getEndTimeFromTask(task, actualTime ?: timeSlot.displayTime)
                    textTimeRange.text = "${actualTime ?: timeSlot.displayTime} - $endTime"
                    
                    // Set indicator color based on task status
                    val indicatorColor = when (task.status) {
                        TodoStatus.COMPLETED -> com.example.noteapp.R.color.status_completed
                        TodoStatus.IN_PROGRESS -> com.example.noteapp.R.color.status_in_progress
                        TodoStatus.IN_REVIEW -> com.example.noteapp.R.color.status_in_progress
                        TodoStatus.TODO -> com.example.noteapp.R.color.status_todo
                        TodoStatus.DONE -> com.example.noteapp.R.color.status_completed
                        TodoStatus.ON_HOLD -> com.example.noteapp.R.color.status_on_hold
                        TodoStatus.CANCELLED -> com.example.noteapp.R.color.status_cancelled
                    }
                    timeIndicator.setBackgroundResource(indicatorColor)
                    
                    // Set completion state
                    val alpha = if (task.status == TodoStatus.COMPLETED) 0.6f else 1.0f
                    textTitle.alpha = alpha
                    textTimeRange.alpha = alpha
                    
                    if (task.status == TodoStatus.COMPLETED) {
                        textTitle.paintFlags = textTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        textTitle.paintFlags = textTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                    
                    root.setOnClickListener { onTodoClick(task) }
                    root.alpha = 1.0f
                }
            }
        }
        
        private fun extractActualTimeFromTask(task: ToDo): String? {
            // Priority order: reminderTime -> dueTime -> createdTime
            val timeString = when {
                !task.reminderTime.isNullOrEmpty() -> task.reminderTime
                !task.dueTime.isNullOrEmpty() -> task.dueTime
                else -> task.createdTime
            }
            
            return try {
                // Check if time string contains time part (HH:mm)
                if (timeString?.contains(" ") == true && timeString.split(" ").size >= 2) {
                    val timePart = timeString.split(" ")[1] // Get "HH:mm" part
                    val hour = timePart.split(":")[0].toInt()
                    val minute = timePart.split(":")[1].toInt()
                    
                    // Format to 12-hour format
                    when {
                        hour == 0 -> "12:${String.format("%02d", minute)} AM"
                        hour < 12 -> "$hour:${String.format("%02d", minute)} AM"
                        hour == 12 -> "12:${String.format("%02d", minute)} PM"
                        else -> "${hour - 12}:${String.format("%02d", minute)} PM"
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
        
        private fun getEndTimeFromTask(task: ToDo, startTime: String): String {
            return try {
                // Use estimated duration if available
                if (task.estimatedDuration > 0) {
                    val durationHours = task.estimatedDuration / 60
                    val durationMinutes = task.estimatedDuration % 60
                    
                    // Parse start time to calculate end time
                    val startHour = extractHourFromTimeString(startTime)
                    val startMinute = extractMinuteFromTimeString(startTime)
                    
                    if (startHour != null && startMinute != null) {
                        val endHour = startHour + durationHours
                        val endMinute = startMinute + durationMinutes
                        
                        val finalHour = if (endMinute >= 60) endHour + 1 else endHour
                        val finalMinute = endMinute % 60
                        
                        // Format to 12-hour format
                        when {
                            finalHour == 0 -> "12:${String.format("%02d", finalMinute)} AM"
                            finalHour < 12 -> "$finalHour:${String.format("%02d", finalMinute)} AM"
                            finalHour == 12 -> "12:${String.format("%02d", finalMinute)} PM"
                            finalHour < 24 -> "${finalHour - 12}:${String.format("%02d", finalMinute)} PM"
                            else -> "${(finalHour % 24) - 12}:${String.format("%02d", finalMinute)} PM"
                        }
                    } else {
                        getEndTime(extractHourFromDisplayTime(startTime))
                    }
                } else {
                    // Default 1 hour duration
                    getEndTime(extractHourFromDisplayTime(startTime))
                }
            } catch (e: Exception) {
                getEndTime(extractHourFromDisplayTime(startTime))
            }
        }
        
        private fun extractHourFromTimeString(timeString: String): Int? {
            return try {
                val cleanTime = timeString.replace(" AM", "").replace(" PM", "")
                val hour = cleanTime.split(":")[0].toInt()
                if (timeString.contains("PM") && hour != 12) hour + 12
                else if (timeString.contains("AM") && hour == 12) 0
                else hour
            } catch (e: Exception) {
                null
            }
        }
        
        private fun extractMinuteFromTimeString(timeString: String): Int? {
            return try {
                val cleanTime = timeString.replace(" AM", "").replace(" PM", "")
                if (cleanTime.contains(":")) {
                    cleanTime.split(":")[1].toInt()
                } else {
                    0
                }
            } catch (e: Exception) {
                null
            }
        }
        
        private fun extractHourFromDisplayTime(displayTime: String): Int {
            return try {
                val cleanTime = displayTime.replace(" AM", "").replace(" PM", "")
                val hour = cleanTime.split(":")[0].toInt()
                if (displayTime.contains("PM") && hour != 12) hour + 12
                else if (displayTime.contains("AM") && hour == 12) 0
                else hour
            } catch (e: Exception) {
                9
            }
        }
        
        private fun getEndTime(startHour: Int): String {
            val endHour = startHour + 1
            return when {
                endHour == 0 -> "12 AM"
                endHour < 12 -> "$endHour AM"
                endHour == 12 -> "12 PM"
                else -> "${endHour - 12} PM"
            }
        }
    }

    class TimeSlotDiffCallback : DiffUtil.ItemCallback<TimeSlot>() {
        override fun areItemsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
            return oldItem.hour == newItem.hour
        }

        override fun areContentsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
            return oldItem == newItem
        }
    }
}
