package com.example.noteapp.model

data class TimeSlot(
    val hour: Int,
    val displayTime: String,
    val tasks: List<ToDo> = emptyList()
) {
    companion object {
        fun generateTimeSlots(): List<TimeSlot> {
            val timeSlots = mutableListOf<TimeSlot>()
            
            // Generate time slots from 6 AM to 11 PM
            for (hour in 6..23) {
                val displayTime = when {
                    hour == 0 -> "12 AM"
                    hour < 12 -> "$hour AM"
                    hour == 12 -> "12 PM"
                    else -> "${hour - 12} PM"
                }
                
                timeSlots.add(TimeSlot(hour, displayTime))
            }
            
            return timeSlots
        }
    }
}
