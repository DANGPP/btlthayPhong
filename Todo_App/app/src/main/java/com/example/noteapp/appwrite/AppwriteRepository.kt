package com.example.noteapp.appwrite

import android.content.Context
import android.util.Log
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import io.appwrite.services.Databases
import com.example.noteapp.model.User
import com.example.noteapp.model.ToDo
import com.example.noteapp.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import io.appwrite.ID

class AppwriteRepository(private val context: Context) {
    private val databases: Databases = AppwriteConfig.getDatabases(context)
    
    companion object {
        private const val TAG = "AppwriteRepository"
    }
    
    // Test connection to Appwrite
    suspend fun testConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Try to list documents from user collection to test connection
                databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.USER_COLLECTION_ID,
                    queries = listOf(Query.limit(1))
                )
                Log.d(TAG, "Appwrite connection successful")
                true
            } catch (e: AppwriteException) {
                Log.e(TAG, "Appwrite connection failed: ${e.message}")
                false
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during connection test: ${e.message}")
                false
            }
        }
    }
    
    // Create a new user
    suspend fun createUser(user: User): User? {
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "createdAt" to user.createdAt,
                    "updatedAt" to user.updatedAt
                )
                
                val document = databases.createDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.USER_COLLECTION_ID,
                    documentId = "unique()",
                    data = data
                )
                
                Log.d(TAG, "User created successfully: ${document.id}")
                documentToUser(document)
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to create user: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error creating user: ${e.message}")
                null
            }
        }
    }
    
    // Get user by ID
    suspend fun getUserById(userId: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val document = databases.getDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.USER_COLLECTION_ID,
                    documentId = userId
                )
                
                Log.d(TAG, "User retrieved successfully: ${document.id}")
                documentToUser(document)
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get user: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting user: ${e.message}")
                null
            }
        }
    }
    
    // Get all users
    suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.USER_COLLECTION_ID
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} users")
                documents.documents.mapNotNull { documentToUser(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get all users: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting all users: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Update user
    suspend fun updateUser(userId: String, user: User): User? {
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "updatedAt" to user.updatedAt
                )
                
                val document = databases.updateDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.USER_COLLECTION_ID,
                    documentId = userId,
                    data = data
                )
                
                Log.d(TAG, "User updated successfully: ${document.id}")
                documentToUser(document)
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to update user: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating user: ${e.message}")
                null
            }
        }
    }
    
    // Delete user
    suspend fun deleteUser(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                databases.deleteDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.USER_COLLECTION_ID,
                    documentId = userId
                )
                
                Log.d(TAG, "User deleted successfully: $userId")
                true
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to delete user: ${e.message}")
                false
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error deleting user: ${e.message}")
                false
            }
        }
    }
    
    // Helper function to convert Document to User
    private fun documentToUser(document: Document<Map<String, Any>>): User? {
        return try {
            User(
                id = document.id,
                name = document.data["name"] as? String ?: "",
                email = document.data["email"] as? String ?: "",
                createdAt = document.data["createdAt"] as? String ?: "",
                updatedAt = document.data["updatedAt"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting document to user: ${e.message}")
            null
        }
    }
    
    // ==================== TODO CRUD OPERATIONS ====================
    
    // Create a new todo
    suspend fun createTodo(todo: ToDo): ToDo? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating todo: $todo")
                val data = mapOf(
                    "title" to todo.title,
                    "description" to todo.description,
                    "createdTime" to todo.createdTime,
                    "dueTime" to todo.dueTime,
                    "completedDate" to todo.completedDate,
                    "userId" to todo.userId,
                    "status" to todo.status.value,
                    "priority" to todo.priority.value,
                    "category" to todo.category,
                    "reminderTime" to todo.reminderTime,
                    "estimatedDuration" to todo.estimatedDuration,
                    "actualDuration" to todo.actualDuration
                )
                
                Log.d(TAG, "Todo data to create: $data")
                val document = databases.createDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                    documentId = ID.unique(),
                    data = data
                )
                
                Log.d(TAG, "Todo created successfully with ID: ${document.id}")
                documentToTodo(document)
            } catch (e: AppwriteException) {
                Log.e(TAG, "AppwriteException creating todo: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Exception creating todo: ${e.message}")
                null
            }
        }
    }
    
    // Get todo by ID
    suspend fun getTodoById(todoId: String): ToDo? {
        return withContext(Dispatchers.IO) {
            try {
                val document = databases.getDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                    documentId = todoId
                )
                
                Log.d(TAG, "Todo retrieved successfully: ${document.id}")
                documentToTodo(document)
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get todo: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting todo: ${e.message}")
                null
            }
        }
    }
    
    // Get all todos for a specific user
    suspend fun getAllTodosByUserId(userId: String): List<ToDo> {
        return try {
            Log.d(TAG, "Fetching todos for userId: $userId")
            val documents = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                queries = listOf(Query.equal("userId", userId))
            )
            Log.d(TAG, "Retrieved ${documents.documents.size} todos for user: $userId")
            documents.documents.mapNotNull { documentToTodo(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Get all todos for a user sorted by created time (DESC)
    suspend fun getAllTodosSortedByCreatedTimeDESC(userId: String): List<ToDo> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.orderDesc("createdTime")
                    )
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} todos sorted by created time DESC")
                documents.documents.mapNotNull { documentToTodo(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get sorted todos: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting sorted todos: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Get all todos for a user sorted by created time (ASC)
    suspend fun getAllTodosSortedByCreatedTimeASC(userId: String): List<ToDo> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.orderAsc("createdTime")
                    )
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} todos sorted by created time ASC")
                documents.documents.mapNotNull { documentToTodo(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get sorted todos: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting sorted todos: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Search todos by title or description
    suspend fun searchTodos(userId: String, searchQuery: String): List<ToDo> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.search("title", searchQuery)
                    )
                )
                
                Log.d(TAG, "Found ${documents.documents.size} todos matching search: $searchQuery")
                documents.documents.mapNotNull { documentToTodo(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to search todos: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error searching todos: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Get todos by completion status (backward compatibility)
    suspend fun getTodosByCompletionStatus(userId: String, isCompleted: Boolean): List<ToDo> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.equal("isCompleted", isCompleted),
                        Query.orderDesc("createdTime")
                    )
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} todos with completion status: $isCompleted")
                documents.documents.mapNotNull { documentToTodo(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get todos by completion status: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting todos by completion status: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Get todos by status
    suspend fun getTodosByStatus(userId: String, status: com.example.noteapp.model.TodoStatus): List<ToDo> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.equal("status", status.value),
                        Query.orderDesc("createdTime")
                    )
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} todos with status: ${status.displayName}")
                documents.documents.mapNotNull { documentToTodo(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get todos by status: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting todos by status: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Get todos by priority
    suspend fun getTodosByPriority(userId: String, priority: com.example.noteapp.model.TodoPriority): List<ToDo> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.equal("priority", priority.value),
                        Query.orderDesc("createdTime")
                    )
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} todos with priority: ${priority.displayName}")
                documents.documents.mapNotNull { documentToTodo(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get todos by priority: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting todos by priority: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Get todos by category
    suspend fun getTodosByCategory(userId: String, category: String): List<ToDo> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.equal("category", category),
                        Query.orderDesc("createdTime")
                    )
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} todos in category: $category")
                documents.documents.mapNotNull { documentToTodo(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get todos by category: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting todos by category: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Get todos due today
    suspend fun getTodosDueToday(userId: String, todayDate: String): List<ToDo> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.equal("isCompleted", false),
                        Query.startsWith("dueTime", todayDate)
                    )
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} todos due today")
                documents.documents.mapNotNull { documentToTodo(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get todos due today: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting todos due today: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Update todo
    suspend fun updateTodo(todoId: String, todo: ToDo): ToDo? {
        return try {
            val data = mapOf(
                "title" to todo.title,
                "description" to todo.description,
                "dueTime" to todo.dueTime,
                "completedDate" to todo.completedDate,
                "status" to todo.status.value,
                "priority" to todo.priority.value,
                "category" to todo.category,
                "reminderTime" to todo.reminderTime,
                "estimatedDuration" to todo.estimatedDuration,
                "actualDuration" to todo.actualDuration
            )
            
            val document = databases.updateDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                documentId = todoId,
                data = data
            )
            
            documentToTodo(document)
        } catch (e: Exception) {
            null
        }
    }
    
    // Delete todo
    suspend fun deleteTodo(todoId: String): Boolean {
        return try {
            databases.deleteDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.TODO_COLLECTION_ID,
                documentId = todoId
            )
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Helper function to convert Document to Todo
    private fun documentToTodo(document: Document<Map<String, Any>>): ToDo? {
        return try {
            ToDo(
                id = document.id,
                title = document.data["title"] as? String ?: "",
                description = document.data["description"] as? String ?: "",
                createdTime = document.data["createdTime"] as? String ?: "",
                dueTime = document.data["dueTime"] as? String,
                completedDate = document.data["completedDate"] as? String,
                userId = document.data["userId"] as? String ?: "",
                status = com.example.noteapp.model.TodoStatus.fromValue(document.data["status"] as? String ?: "todo"),
                priority = com.example.noteapp.model.TodoPriority.fromValue(document.data["priority"] as? String ?: "medium"),
                category = document.data["category"] as? String ?: "General",
                reminderTime = document.data["reminderTime"] as? String,
                estimatedDuration = (document.data["estimatedDuration"] as? Number)?.toInt() ?: 0,
                actualDuration = (document.data["actualDuration"] as? Number)?.toInt() ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting document to todo: ${e.message}")
            null
        }
    }
    
    // ==================== NOTE CRUD OPERATIONS ====================
    
    // Create a new note
    suspend fun createNote(note: Note): Note? {
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "title" to note.title,
                    "description" to note.description,
                    "createdTime" to note.createdTime,
                    "userId" to note.userId
                )
                
                val document = databases.createDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.NOTE_COLLECTION_ID,
                    documentId = ID.unique(),
                    data = data
                )
                
                Log.d(TAG, "Note created successfully: ${document.id}")
                documentToNote(document)
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to create note: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error creating note: ${e.message}")
                null
            }
        }
    }
    
    // Get note by ID
    suspend fun getNoteById(noteId: String): Note? {
        return withContext(Dispatchers.IO) {
            try {
                val document = databases.getDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.NOTE_COLLECTION_ID,
                    documentId = noteId
                )
                
                Log.d(TAG, "Note retrieved successfully: ${document.id}")
                documentToNote(document)
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get note: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting note: ${e.message}")
                null
            }
        }
    }
    
    // Get all notes for a specific user
    suspend fun getAllNotesByUserId(userId: String): List<Note> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.NOTE_COLLECTION_ID,
                    queries = listOf(Query.equal("userId", userId))
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} notes for user: $userId")
                documents.documents.mapNotNull { documentToNote(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get notes for user: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting notes for user: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Search notes by title or description
    suspend fun searchNotes(userId: String, searchQuery: String): List<Note> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.NOTE_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.search("title", searchQuery)
                    )
                )
                
                Log.d(TAG, "Found ${documents.documents.size} notes matching search: $searchQuery")
                documents.documents.mapNotNull { documentToNote(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to search notes: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error searching notes: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Get all notes sorted by created time (ASC)
    suspend fun getAllNotesSortedByCreatedTimeASC(userId: String): List<Note> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.NOTE_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.orderAsc("createdTime")
                    )
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} notes sorted by created time ASC")
                documents.documents.mapNotNull { documentToNote(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get sorted notes: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting sorted notes: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Get all notes sorted by created time (DESC)
    suspend fun getAllNotesSortedByCreatedTimeDESC(userId: String): List<Note> {
        return withContext(Dispatchers.IO) {
            try {
                val documents = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.NOTE_COLLECTION_ID,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.orderDesc("createdTime")
                    )
                )
                
                Log.d(TAG, "Retrieved ${documents.documents.size} notes sorted by created time DESC")
                documents.documents.mapNotNull { documentToNote(it) }
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to get sorted notes: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error getting sorted notes: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Update note
    suspend fun updateNote(noteId: String, note: Note): Note? {
        return withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "title" to note.title,
                    "description" to note.description
                )
                
                val document = databases.updateDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.NOTE_COLLECTION_ID,
                    documentId = noteId,
                    data = data
                )
                
                Log.d(TAG, "Note updated successfully: ${document.id}")
                documentToNote(document)
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to update note: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating note: ${e.message}")
                null
            }
        }
    }
    
    // Delete note
    suspend fun deleteNote(noteId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                databases.deleteDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.NOTE_COLLECTION_ID,
                    documentId = noteId
                )
                
                Log.d(TAG, "Note deleted successfully: $noteId")
                true
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to delete note: ${e.message}")
                false
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error deleting note: ${e.message}")
                false
            }
        }
    }
    
    // Helper function to convert Document to Note
    private fun documentToNote(document: Document<Map<String, Any>>): Note? {
        return try {
            Note(
                id = document.id,
                title = document.data["title"] as? String ?: "",
                description = document.data["description"] as? String ?: "",
                createdTime = document.data["createdTime"] as? String ?: "",
                userId = document.data["userId"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting document to note: ${e.message}")
            null
        }
    }
    
    // ==================== AI BATCH OPERATIONS ====================
    
    // Create multiple todos in batch (for AI-generated todos)
    suspend fun createTodosBatch(todos: List<ToDo>): List<ToDo> {
        return withContext(Dispatchers.IO) {
            val createdTodos = mutableListOf<ToDo>()
            
            for (todo in todos) {
                try {
                    val createdTodo = createTodo(todo)
                    createdTodo?.let { createdTodos.add(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create todo in batch: ${todo.title}, error: ${e.message}")
                }
            }
            
            Log.d(TAG, "Batch created ${createdTodos.size} out of ${todos.size} todos")
            createdTodos
        }
    }
    
    // Check for scheduling conflicts
    suspend fun checkSchedulingConflicts(userId: String, newTodos: List<ToDo>): List<ToDo> {
        return withContext(Dispatchers.IO) {
            try {
                // Get existing todos for the user
                val existingTodos = getAllTodosByUserId(userId)
                val conflictingTodos = mutableListOf<ToDo>()
                
                for (newTodo in newTodos) {
                    if (newTodo.dueTime.isNullOrBlank()) continue
                    
                    val hasConflict = existingTodos.any { existingTodo ->
                        !existingTodo.dueTime.isNullOrBlank() && 
                        existingTodo.dueTime == newTodo.dueTime &&
                        existingTodo.status != com.example.noteapp.model.TodoStatus.COMPLETED
                    }
                    
                    if (hasConflict) {
                        conflictingTodos.add(newTodo)
                    }
                }
                
                Log.d(TAG, "Found ${conflictingTodos.size} scheduling conflicts")
                conflictingTodos
            } catch (e: Exception) {
                Log.e(TAG, "Error checking scheduling conflicts: ${e.message}")
                emptyList()
            }
        }
    }
}
