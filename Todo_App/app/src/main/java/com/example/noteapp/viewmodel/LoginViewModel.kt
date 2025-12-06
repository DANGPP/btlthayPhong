package com.example.noteapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.appwrite.AuthResult
import com.example.noteapp.auth.CustomAuthManager
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
    private val authManager = CustomAuthManager(appContext)

    private val _state = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val state: LiveData<AuthUiState> = _state

    fun login(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authManager.login(email, password)) {
                is AuthResult.Success -> {
                    _state.value = AuthUiState.Success("Đăng nhập thành công")
                }
                is AuthResult.Error -> {
                    _state.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return authManager.isLoggedIn()
    }

    fun clearState() {
        _state.value = AuthUiState.Idle
    }
}
