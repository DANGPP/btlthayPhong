package com.example.noteapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp.appwrite.AppwriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AppwriteRepository) : ViewModel() {
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _passwordChangeSuccess = MutableLiveData<Boolean>()
    val passwordChangeSuccess: LiveData<Boolean> = _passwordChangeSuccess
    
    fun updateUserName(userId: String, newName: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        val success = repository.updateUserName(userId, newName)
        if (!success) {
            _error.postValue("Không thể cập nhật tên")
        }
        _isLoading.postValue(false)
    }
    
    fun changePassword(userId: String, currentPassword: String, newPassword: String) = 
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val success = repository.changePassword(userId, currentPassword, newPassword)
            if (success) {
                _passwordChangeSuccess.postValue(true)
            } else {
                _error.postValue("Mật khẩu hiện tại không đúng")
            }
            _isLoading.postValue(false)
        }
    
    fun clearError() {
        _error.value = null
    }
    
    class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(AppwriteRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
