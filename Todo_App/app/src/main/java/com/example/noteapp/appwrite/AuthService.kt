package com.example.noteapp.appwrite

import android.content.Context
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.User
import io.appwrite.services.Account

class AuthService(private val context: Context) {
    private val account: Account

    init {
        AppwriteConfig.init(context)
        account = AppwriteConfig.account
    }

    // Register new user and automatically login
    suspend fun register(email: String, password: String, name: String): AuthResult {
        return try {
            // First, create the user account
            account.create(
                userId = ID.unique(),
                email = email,
                password = password,
                name = name
            )
            
            // Then automatically login to create a session
            account.createEmailPasswordSession(
                email = email,
                password = password
            )
            
            // Get the logged in user data
            val user = account.get()
            AuthResult.Success(user)
        } catch (e: AppwriteException) {
            // Map Appwrite exception message
            AuthResult.Error(e.message ?: "Registration failed")
        } catch (e: Exception) {
            AuthResult.Error("Registration failed")
        }
    }

    // Login user
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            // Avoid logging sensitive data like full email/password in production
            account.createEmailPasswordSession(
                email = email,
                password = password
            )
            val user = account.get()
            AuthResult.Success(user)
        } catch (e: AppwriteException) {
            // Do not expose internal details to logs in production; return user-friendly message
            AuthResult.Error(e.message ?: "Login failed")
        } catch (e: Exception) {
            AuthResult.Error("Login failed")
        }
    }

    // Logout user
    suspend fun logout(): Boolean {
        return try {
            account.deleteSession("current")
            true
        } catch (e: Exception) {
            false
        }
    }

    // Force logout and login - useful when session conflicts occur
    suspend fun forceLogin(email: String, password: String): AuthResult {
        return try {
            // Clear any existing sessions first
            try {
                account.deleteSessions()
            } catch (e: Exception) {
                // Ignore if no sessions to delete
            }

            account.createEmailPasswordSession(
                email = email,
                password = password
            )
            val user = account.get()
            AuthResult.Success(user)
        } catch (e: AppwriteException) {
            AuthResult.Error(e.message ?: "Login failed")
        } catch (e: Exception) {
            AuthResult.Error("Login failed")
        }
    }

    // Check if user is logged in
    suspend fun isLoggedIn(): Boolean {
        return try {
            account.get()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get current user
    suspend fun getCurrentUser(): User<Map<String, Any>>? {
        return try {
            val user = account.get()
            user
        } catch (e: Exception) {
           null
        }
    }


    // Get current user ID with all authentication handling
    // Returns null if user is not authenticated (and handles cleanup internally)
    suspend fun getCurrentUserId(): String? {
        return getCurrentUser()?.id
    }
}

// Sealed class for authentication results
sealed class AuthResult {
    data class Success(val user: User<Map<String, Any>>) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
