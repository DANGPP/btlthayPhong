package com.example.noteapp.model

import com.example.noteapp.R
import java.io.Serializable

/**
 * WorkspaceTask - Model riêng cho tasks trong workspace
 * Tách biệt hoàn toàn với personal ToDo
 */
data class WorkspaceTask(
    var id: String = "",
    var title: String,
    var description: String = "",
    var workspaceId: String, // Required - always belongs to a workspace
    var createdBy: String, // User ID of creator
    var assignedTo: List<String> = emptyList(), // List of user IDs
    var status: TodoStatus = TodoStatus.TODO,
    var priority: TodoPriority = TodoPriority.MEDIUM,
    var category: String = "Chung",
    var dueDate: String? = null, // Format: dd/MM/yyyy
    var dueTime: String? = null, // Format: HH:mm
    var estimatedHours: Int = 0,
    var actualHours: Int = 0,
    var attachments: List<String> = emptyList(), // URLs or file IDs
    var comments: List<String> = emptyList(), // Comment IDs or texts
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) : Serializable {

    // Check if current user is assigned
    fun isAssignedTo(userId: String): Boolean {
        return assignedTo.contains(userId)
    }
    
    // Check if current user is creator
    fun isCreatedBy(userId: String): Boolean {
        return createdBy == userId
    }
    
    // Get board column for this task
    fun getBoardColumn(): BoardColumn {
        return status.toBoardColumn()
    }
}
