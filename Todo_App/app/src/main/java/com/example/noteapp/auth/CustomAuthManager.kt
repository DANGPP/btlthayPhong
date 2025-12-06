package com.example.noteapp.auth

import android.content.Context
import android.util.Log
import com.example.noteapp.appwrite.AppwriteConfig
import com.example.noteapp.appwrite.AuthResult
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Custom Authentication Manager
 * Quản lý đăng nhập/đăng ký không dùng Appwrite Auth
 */
class CustomAuthManager(private val context: Context) {
    private val databases: Databases = AppwriteConfig.getDatabases(context)
    private val sessionManager = SessionManager(context)
    
    companion object {
        private const val TAG = "CustomAuthManager"
    }
    
    /**
     * Hash password using SHA-256
     */
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
    
    /**
     * Register new user
     */
    suspend fun register(name: String, email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if email already exists
                val existingUsers = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.USER_COLLECTION_ID,
                    queries = listOf(Query.equal("email", email))
                )
                
                if (existingUsers.documents.isNotEmpty()) {
                    return@withContext AuthResult.Error("Email đã được sử dụng")
                }
                
                // Hash password
                val hashedPassword = hashPassword(password)
                
                // Create user document
                val document = databases.createDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.USER_COLLECTION_ID,
                    documentId = ID.unique(),
                    data = mapOf(
                        "email" to email,
                        "password" to hashedPassword,
                        "name" to name
                    )
                )
                
                // Save session
                sessionManager.saveCurrentUserId(document.id)
                sessionManager.saveUserEmail(email)
                sessionManager.saveUserName(name)
                
                Log.d(TAG, "User registered successfully: ${document.id}")
                AuthResult.Success(io.appwrite.models.User(
                    id = document.id,
                    createdAt = document.createdAt,
                    updatedAt = document.updatedAt,
                    name = name,
                    password = "",
                    hash = "",
                    hashOptions = null,
                    registration = document.createdAt,
                    status = true,
                    labels = emptyList(),
                    passwordUpdate = document.createdAt,
                    email = email,
                    phone = "",
                    emailVerification = false,
                    phoneVerification = false,
                    mfa = false,
                    prefs = io.appwrite.models.Preferences(emptyMap()),
                    targets = emptyList(),
                    accessedAt = document.createdAt
                ))
            } catch (e: AppwriteException) {
                Log.e(TAG, "Registration failed: ${e.message}")
                AuthResult.Error(e.message ?: "Đăng ký thất bại")
            } catch (e: Exception) {
                Log.e(TAG, "Registration failed: ${e.message}")
                AuthResult.Error("Đăng ký thất bại")
            }
        }
    }
    
    /**
     * Login user
     */
    suspend fun login(email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                // Find user by email
                val users = databases.listDocuments(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.USER_COLLECTION_ID,
                    queries = listOf(Query.equal("email", email))
                )
                
                if (users.documents.isEmpty()) {
                    return@withContext AuthResult.Error("Email hoặc mật khẩu không đúng")
                }
                
                val userDoc = users.documents.first()
                val storedPassword = userDoc.data["password"] as? String
                val hashedPassword = hashPassword(password)
                
                // Verify password
                if (storedPassword != hashedPassword) {
                    return@withContext AuthResult.Error("Email hoặc mật khẩu không đúng")
                }
                
                // Get user info
                val name = userDoc.data["name"] as? String ?: ""
                
                // Save session
                sessionManager.saveCurrentUserId(userDoc.id)
                sessionManager.saveUserEmail(email)
                sessionManager.saveUserName(name)
                
                Log.d(TAG, "User logged in successfully: ${userDoc.id}")
                AuthResult.Success(io.appwrite.models.User(
                    id = userDoc.id,
                    createdAt = userDoc.createdAt,
                    updatedAt = userDoc.updatedAt,
                    name = name,
                    password = "",
                    hash = "",
                    hashOptions = null,
                    registration = userDoc.createdAt,
                    status = true,
                    labels = emptyList(),
                    passwordUpdate = userDoc.createdAt,
                    email = email,
                    phone = "",
                    emailVerification = false,
                    phoneVerification = false,
                    mfa = false,
                    prefs = io.appwrite.models.Preferences(emptyMap()),
                    targets = emptyList(),
                    accessedAt = userDoc.createdAt
                ))
            } catch (e: AppwriteException) {
                Log.e(TAG, "Login failed: ${e.message}")
                AuthResult.Error(e.message ?: "Đăng nhập thất bại")
            } catch (e: Exception) {
                Log.e(TAG, "Login failed: ${e.message}")
                AuthResult.Error("Đăng nhập thất bại")
            }
        }
    }
    
    /**
     * Logout user
     */
    suspend fun logout(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                sessionManager.clearSession()
                Log.d(TAG, "User logged out successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Logout failed: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sessionManager.getCurrentUserId() != null
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return sessionManager.getCurrentUserId()
    }
}
