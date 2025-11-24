package com.example.noteapp.model

import com.example.noteapp.R
import java.io.Serializable

data class ToDo(
    var id: String = "",
    var title: String,
    var description: String = "",
    var createdTime: String,
    var dueTime: String?,
    var completedDate: String?,
    var userId: String = "", // Link to the authenticated user
    var status: TodoStatus = TodoStatus.TODO,
    var priority: TodoPriority = TodoPriority.MEDIUM,
    var category: String = "General",
    var reminderTime: String? = null,
    var estimatedDuration: Int = 0, // in minutes
    var actualDuration: Int = 0, // in minutes for completed tasks
    var workspaceId: String? = null, // Link to workspace (null = personal task)
    var assignedTo: List<String> = emptyList(), // List of user IDs assigned to this task
    var createdBy: String = "" // User who created the task
) : Serializable {

    // Computed property for backward compatibility
    val isCompleted: Boolean
        get() = status == TodoStatus.COMPLETED
    
    // Check if this is a shared task
    val isShared: Boolean
        get() = !workspaceId.isNullOrEmpty()

    // Constructor for creating new todos (without ID)
    constructor(
        title: String,
        description: String = "",
        createdTime: String,
        dueTime: String?,
        completedDate: String?,
        userId: String,
        status: TodoStatus = TodoStatus.TODO,
        priority: TodoPriority = TodoPriority.MEDIUM,
        category: String = "General",
        reminderTime: String? = null,
        estimatedDuration: Int = 0,
        actualDuration: Int = 0,
        workspaceId: String? = null,
        assignedTo: List<String> = emptyList(),
        createdBy: String = ""
    ) : this(
        "",
        title,
        description,
        createdTime,
        dueTime,
        completedDate,
        userId,
        status,
        priority,
        category,
        reminderTime,
        estimatedDuration,
        actualDuration,
        workspaceId,
        assignedTo,
        createdBy
    )
}

enum class TodoStatus(val value: String, val displayName: String, val colorRes: Int) {
    TODO("to_do", "To Do", R.color.status_todo),
    IN_PROGRESS("in_progress", "In Progress", R.color.status_in_progress),
    COMPLETED("completed", "Completed", R.color.status_completed),
    CANCELLED("cancelled", "Cancelled", R.color.status_cancelled),
    ON_HOLD("on_hold", "On Hold", R.color.status_on_hold);
    
    companion object {
        fun fromValue(value: String): TodoStatus {
            return values().find { it.value == value } ?: TODO
        }
    }
}

enum class TodoPriority(val value: String, val displayName: String) {
    LOW("low", "Low"),
    MEDIUM("medium", "Medium"),
    HIGH("high", "High"),
    URGENT("urgent", "Urgent");

    companion object {
        fun fromValue(value: String): TodoPriority {
            return values().find { it.value == value } ?: MEDIUM
        }
    }
}