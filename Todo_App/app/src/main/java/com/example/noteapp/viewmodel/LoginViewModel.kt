package com.example.noteapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.appwrite.AuthResult
import com.example.noteapp.auth.AuthRepositoryImpl
import com.example.noteapp.auth.SessionManager
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String = "") : AuthUiState()
    data class Error(val error: String) : AuthUiState()
}

class LoginViewModel(context: Context) : ViewModel() {
    // Use applicationContext to avoid leaking Activity/Fragment context
    private val appContext = context.applicationContext
    private val repository = AuthRepositoryImpl(appContext)
    private val sessionManager = SessionManager(appContext)

    private val _state = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val state: LiveData<AuthUiState> = _state

    fun login(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                is AuthResult.Success -> {
                    // Save current user id in session
                    sessionManager.saveCurrentUserId(result.user.id)
                    _state.value = AuthUiState.Success("Login successful")
                }
                is AuthResult.Error -> {
                    _state.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    suspend fun isLoggedIn(): Boolean {
        // First check session storage
        val stored = sessionManager.getCurrentUserId()
        if (!stored.isNullOrBlank()) return true
        // Fallback to remote check
        return repository.isLoggedIn()
    }

    fun clearState() {
        _state.value = AuthUiState.Idle
    }
}
