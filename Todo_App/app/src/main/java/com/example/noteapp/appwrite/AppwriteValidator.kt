package com.example.noteapp.appwrite

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class to validate Appwrite configuration
 * Use this to check if your Appwrite setup is correct before running the app
 */
class AppwriteValidator(private val context: Context) {
    
    companion object {
        private const val TAG = "AppwriteValidator"
    }
    
    /**
     * Validates the Appwrite configuration
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    suspend fun validateConfiguration(): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            val errors = mutableListOf<String>()
            
            // Check if configuration values are still default
            if (AppwriteConfig.PROJECT_ID == "6908ccdf00223cfe80cd") {
                Log.w(TAG, "⚠️ PROJECT_ID might still be default value. Please check AppwriteConfig.kt")
            }
            
            if (AppwriteConfig.DATABASE_ID == "6908cde40006b4bbd549") {
                Log.w(TAG, "⚠️ DATABASE_ID might still be default value. Please check AppwriteConfig.kt")
            }
            
            // Try to connect to Appwrite
            try {
                val repository = AppwriteRepository(context)
                val connectionSuccess = repository.testConnection()
                
                if (connectionSuccess) {
                    Log.d(TAG, "✅ Successfully connected to Appwrite")
                } else {
                    errors.add("Failed to connect to Appwrite. Check your Project ID and Database ID.")
                }
            } catch (e: Exception) {
                errors.add("Error connecting to Appwrite: ${e.message}")
                Log.e(TAG, "❌ Appwrite connection error", e)
            }
            
            // Return result
            if (errors.isEmpty()) {
                Pair(true, "✅ Appwrite configuration is valid!")
            } else {
                Pair(false, errors.joinToString("\n"))
            }
        }
    }
    
    /**
     * Quick validation that prints results to log
     */
    suspend fun quickValidate() {
        Log.d(TAG, "=== Appwrite Configuration Check ===")
        Log.d(TAG, "Project ID: ${AppwriteConfig.PROJECT_ID}")
        Log.d(TAG, "Database ID: ${AppwriteConfig.DATABASE_ID}")
        Log.d(TAG, "User Collection ID: ${AppwriteConfig.USER_COLLECTION_ID}")
        Log.d(TAG, "Todo Collection ID: ${AppwriteConfig.TODO_COLLECTION_ID}")
        Log.d(TAG, "Note Collection ID: ${AppwriteConfig.NOTE_COLLECTION_ID}")
        
        val (isValid, message) = validateConfiguration()
        
        if (isValid) {
            Log.i(TAG, message)
        } else {
            Log.e(TAG, "❌ Configuration issues found:")
            Log.e(TAG, message)
        }
        Log.d(TAG, "====================================")
    }
}
