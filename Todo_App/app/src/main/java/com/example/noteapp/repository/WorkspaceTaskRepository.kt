package com.example.noteapp.repository

import android.content.Context
import com.example.noteapp.appwrite.AppwriteConfig
import com.example.noteapp.model.TodoPriority
import com.example.noteapp.model.TodoStatus
import com.example.noteapp.model.WorkspaceTask
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkspaceTaskRepository(private val context: Context) {
    
    private val databases = AppwriteConfig.getDatabases(context)
    private val databaseId = AppwriteConfig.DATABASE_ID
    private val collectionId = AppwriteConfig.WORKSPACE_TASK_COLLECTION_ID
    
    // Create workspace task
    suspend fun createTask(task: WorkspaceTask): Result<WorkspaceTask> = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "title" to task.title,
                "description" to task.description,
                "workspaceId" to task.workspaceId,
                "createdBy" to task.createdBy,
                "assignedTo" to task.assignedTo,
                "status" to task.status.name,
                "priority" to task.priority.name,
                "category" to task.category,
                "dueDate" to task.dueDate,
                "dueTime" to task.dueTime,
                "estimatedHours" to task.estimatedHours,
                "actualHours" to task.actualHours,
                "createdAt" to task.createdAt,
                "updatedAt" to task.updatedAt
            )
            
            val document = databases.createDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = "unique()",
                data = data
            )
            
            Result.success(documentToTask(document))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get all tasks in workspace
    suspend fun getWorkspaceTasks(workspaceId: String): Result<List<WorkspaceTask>> = withContext(Dispatchers.IO) {
        try {
            val documents = databases.listDocuments(
                databaseId = databaseId,
                collectionId = collectionId,
                queries = listOf(
                    Query.equal("workspaceId", workspaceId),
                    Query.orderDesc("createdAt")
                )
            )
            
            val tasks = documents.documents.map { documentToTask(it) }
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get tasks assigned to user
    suspend fun getMyTasks(workspaceId: String, userId: String): Result<List<WorkspaceTask>> = withContext(Dispatchers.IO) {
        try {
            val documents = databases.listDocuments(
                databaseId = databaseId,
                collectionId = collectionId,
                queries = listOf(
                    Query.equal("workspaceId", workspaceId),
                    Query.search("assignedTo", userId),
                    Query.orderDesc("createdAt")
                )
            )
            
            val tasks = documents.documents.map { documentToTask(it) }
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update task
    suspend fun updateTask(task: WorkspaceTask): Result<WorkspaceTask> = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "title" to task.title,
                "description" to task.description,
                "assignedTo" to task.assignedTo,
                "status" to task.status.name,
                "priority" to task.priority.name,
                "category" to task.category,
                "dueDate" to task.dueDate,
                "dueTime" to task.dueTime,
                "estimatedHours" to task.estimatedHours,
                "actualHours" to task.actualHours,
                "updatedAt" to System.currentTimeMillis()
            )
            
            val document = databases.updateDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = task.id,
                data = data
            )
            
            Result.success(documentToTask(document))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update task status
    suspend fun updateTaskStatus(taskId: String, status: TodoStatus): Result<WorkspaceTask> = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "status" to status.name,
                "updatedAt" to System.currentTimeMillis()
            )
            
            val document = databases.updateDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = taskId,
                data = data
            )
            
            Result.success(documentToTask(document))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete task
    suspend fun deleteTask(taskId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = taskId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get tasks by status
    suspend fun getTasksByStatus(workspaceId: String, status: TodoStatus): Result<List<WorkspaceTask>> = withContext(Dispatchers.IO) {
        try {
            val documents = databases.listDocuments(
                databaseId = databaseId,
                collectionId = collectionId,
                queries = listOf(
                    Query.equal("workspaceId", workspaceId),
                    Query.equal("status", status.name),
                    Query.orderDesc("createdAt")
                )
            )
            
            val tasks = documents.documents.map { documentToTask(it) }
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Convert Appwrite document to WorkspaceTask
    private fun documentToTask(document: Document<Map<String, Any>>): WorkspaceTask {
        return WorkspaceTask(
            id = document.id,
            title = document.data["title"] as? String ?: "",
            description = document.data["description"] as? String ?: "",
            workspaceId = document.data["workspaceId"] as? String ?: "",
            createdBy = document.data["createdBy"] as? String ?: "",
            assignedTo = (document.data["assignedTo"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            status = TodoStatus.valueOf(document.data["status"] as? String ?: "TODO"),
            priority = TodoPriority.valueOf(document.data["priority"] as? String ?: "MEDIUM"),
            category = document.data["category"] as? String ?: "Chung",
            dueDate = document.data["dueDate"] as? String,
            dueTime = document.data["dueTime"] as? String,
            estimatedHours = (document.data["estimatedHours"] as? Number)?.toInt() ?: 0,
            actualHours = (document.data["actualHours"] as? Number)?.toInt() ?: 0,
            createdAt = (document.data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (document.data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
