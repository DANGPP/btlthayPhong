package com.example.noteapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.appwrite.AuthResult
import com.example.noteapp.auth.CustomAuthManager
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val message: String = "") : RegisterUiState()
    data class Error(val error: String) : RegisterUiState()
}

class RegisterViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val authManager = CustomAuthManager(appContext)

    private val _state = MutableLiveData<RegisterUiState>(RegisterUiState.Idle)
    val state: LiveData<RegisterUiState> = _state

    fun validateInputs(name: String, email: String, password: String, confirmPassword: String): String? {
        if (name.isEmpty()) return "Vui lòng nhập tên"
        if (email.isEmpty()) return "Vui lòng nhập email"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Email không hợp lệ"
        if (password.isEmpty()) return "Vui lòng nhập mật khẩu"
        if (confirmPassword.isEmpty()) return "Vui lòng xác nhận mật khẩu"
        if (password != confirmPassword) return "Mật khẩu không khớp"
        return null
    }

    fun register(name: String, email: String, password: String) {
        _state.value = RegisterUiState.Loading
        viewModelScope.launch {
            when (val result = authManager.register(name, email, password)) {
                is AuthResult.Success -> {
                    _state.value = RegisterUiState.Success("Đăng ký thành công")
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
