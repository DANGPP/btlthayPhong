package com.example.noteapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp.appwrite.AppwriteRepository
import com.example.noteapp.auth.SessionManager
import com.example.noteapp.auth.AuthRepositoryImpl
import com.example.noteapp.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(private val context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val appwriteRepository = AppwriteRepository(appContext)
    private val sessionManager = SessionManager(appContext)
    private val authRepository = AuthRepositoryImpl(appContext)

    private val _allNotes = MutableLiveData<List<Note>>()
    val allNotes: LiveData<List<Note>> = _allNotes
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> = _operationSuccess

    // Get current user ID from SessionManager
    private suspend fun getCurrentUserId(): String? {
        return try {
            sessionManager.getCurrentUserId()
        } catch (e: Exception) {
            _error.postValue("Failed to get current user: ${e.message}")
            null
        }
    }

    // Insert a new note
    fun insertNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val noteWithUserId = note.copy(userId = userId)
                val result = appwriteRepository.createNote(noteWithUserId)
                if (result != null) {
                    _operationSuccess.postValue(true)
                    loadAllNotes() // Refresh the list
                } else {
                    _error.postValue("Failed to create note")
                    _operationSuccess.postValue(false)
                }
            } else {
                _error.postValue("User not authenticated")
                _operationSuccess.postValue(false)
            }
        } catch (e: Exception) {
            _error.postValue("Error creating note: ${e.message}")
            _operationSuccess.postValue(false)
        } finally {
            _isLoading.postValue(false)
        }
    }

    // Delete a note
    fun deleteNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val result = appwriteRepository.deleteNote(note.id)
            if (result) {
                _operationSuccess.postValue(true)
                loadAllNotes() // Refresh the list
            } else {
                _error.postValue("Failed to delete note")
                _operationSuccess.postValue(false)
            }
        } catch (e: Exception) {
            _error.postValue("Error deleting note: ${e.message}")
            _operationSuccess.postValue(false)
        } finally {
            _isLoading.postValue(false)
        }
    }

    // Update a note
    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val result = appwriteRepository.updateNote(note.id, note)
            if (result != null) {
                _operationSuccess.postValue(true)
                loadAllNotes() // Refresh the list
            } else {
                _error.postValue("Failed to update note")
                _operationSuccess.postValue(false)
            }
        } catch (e: Exception) {
            _error.postValue("Error updating note: ${e.message}")
            _operationSuccess.postValue(false)
        } finally {
            _isLoading.postValue(false)
        }
    }

    // Load all notes for current user
    fun loadAllNotes() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val notes = appwriteRepository.getAllNotesByUserId(userId)
                _allNotes.postValue(notes)
                _error.postValue(null)
            } else {
                _error.postValue("User not authenticated")
                _allNotes.postValue(emptyList())
            }
        } catch (e: Exception) {
            _error.postValue("Error loading notes: ${e.message}")
            _allNotes.postValue(emptyList())
        } finally {
            _isLoading.postValue(false)
        }
    }

    // Get all notes (alias for loadAllNotes for compatibility)
    fun getAllNote(): LiveData<List<Note>> {
        loadAllNotes()
        return allNotes
    }

    // Search notes
    fun searchDatabase(searchQuery: String) = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val notes = appwriteRepository.searchNotes(userId, searchQuery)
                _allNotes.postValue(notes)
                _error.postValue(null)
            } else {
                _error.postValue("User not authenticated")
                _allNotes.postValue(emptyList())
            }
        } catch (e: Exception) {
            _error.postValue("Error searching notes: ${e.message}")
            _allNotes.postValue(emptyList())
        } finally {
            _isLoading.postValue(false)
        }
    }

    // Get all notes sorted by created time (ASC)
    fun loadAllNotesSortedByCreatedTimeASC() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val notes = appwriteRepository.getAllNotesSortedByCreatedTimeASC(userId)
                _allNotes.postValue(notes)
                _error.postValue(null)
            } else {
                _error.postValue("User not authenticated")
                _allNotes.postValue(emptyList())
            }
        } catch (e: Exception) {
            _error.postValue("Error loading sorted notes: ${e.message}")
            _allNotes.postValue(emptyList())
        } finally {
            _isLoading.postValue(false)
        }
    }

    // Get all notes sorted by created time (DESC)
    fun loadAllNotesSortedByCreatedTimeDESC() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val notes = appwriteRepository.getAllNotesSortedByCreatedTimeDESC(userId)
                _allNotes.postValue(notes)
                _error.postValue(null)
            } else {
                _error.postValue("User not authenticated")
                _allNotes.postValue(emptyList())
            }
        } catch (e: Exception) {
            _error.postValue("Error loading sorted notes: ${e.message}")
            _allNotes.postValue(emptyList())
        } finally {
            _isLoading.postValue(false)
        }
    }

    // Compatibility methods that return LiveData
    fun getAllSortedCreatedTimeASC(): LiveData<List<Note>> {
        loadAllNotesSortedByCreatedTimeASC()
        return allNotes
    }

    fun getAllSortedCreatedTimeDESC(): LiveData<List<Note>> {
        loadAllNotesSortedByCreatedTimeDESC()
        return allNotes
    }

    // Clear error message
    fun clearError() {
        _error.value = null
    }

    // Clear operation success flag
    fun clearOperationSuccess() {
        _operationSuccess.value = false
    }

    class NoteViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NoteViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}