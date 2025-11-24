package com.example.noteapp.auth

import android.content.Context
import com.example.noteapp.appwrite.AuthResult
import com.example.noteapp.appwrite.AuthService

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(name: String, email: String, password: String): AuthResult
    suspend fun logout(): Boolean
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUserId(): String?
}

class AuthRepositoryImpl(private val context: Context) : AuthRepository {
    // Use applicationContext to avoid leaking UI context
    private val appContext = context.applicationContext
    private val authService = AuthService(appContext)

    override suspend fun login(email: String, password: String): AuthResult {
        return authService.login(email, password)
    }

    override suspend fun register(name: String, email: String, password: String): AuthResult {
        // AuthService.register orders arguments (email, password, name)
        return authService.register(email, password, name)
    }

    override suspend fun logout(): Boolean {
        return authService.logout()
    }

    override suspend fun isLoggedIn(): Boolean {
        return authService.isLoggedIn()
    }

    override suspend fun getCurrentUserId(): String? {
        return authService.getCurrentUserId()
    }
}
