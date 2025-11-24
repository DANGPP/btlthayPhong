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

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val message: String = "") : RegisterUiState()
    data class Error(val error: String) : RegisterUiState()
}

class RegisterViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val repository = AuthRepositoryImpl(appContext)
    private val sessionManager = SessionManager(appContext)

    private val _state = MutableLiveData<RegisterUiState>(RegisterUiState.Idle)
    val state: LiveData<RegisterUiState> = _state

    fun validateInputs(name: String, email: String, password: String, confirmPassword: String): String? {
        if (name.isEmpty()) return "Please enter your full name"
        if (name.length < 2) return "Name must be at least 2 characters"
        if (email.isEmpty()) return "Please enter your email"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Please enter a valid email address"
        if (password.isEmpty()) return "Please enter a password"
        if (password.length < 8) return "Password must be at least 8 characters"
        if (!password.matches(".*[A-Z].*".toRegex())) return "Password must contain at least one uppercase letter"
        if (!password.matches(".*[a-z].*".toRegex())) return "Password must contain at least one lowercase letter"
        if (!password.matches(".*\\d.*".toRegex())) return "Password must contain at least one number"
        if (confirmPassword.isEmpty()) return "Please confirm your password"
        if (password != confirmPassword) return "Passwords do not match"
        return null
    }

    fun register(name: String, email: String, password: String) {
        _state.value = RegisterUiState.Loading
        viewModelScope.launch {
            when (val result = repository.register(name, email, password)) {
                is AuthResult.Success -> {
                    // Save current user id in session and auto-login
                    result.user.id?.let { sessionManager.saveCurrentUserId(it) }
                    _state.value = RegisterUiState.Success("Registration successful")
                }
                is AuthResult.Error -> {
                    _state.value = RegisterUiState.Error(result.message)
                }
            }
        }
    }

    fun clearState() {
        _state.value = RegisterUiState.Idle
    }
}
