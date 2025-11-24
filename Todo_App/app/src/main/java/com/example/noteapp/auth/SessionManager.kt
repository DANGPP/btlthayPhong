package com.example.noteapp.auth

import android.content.Context

class SessionManager(context: Context) {
    private val prefsName = "session_prefs"
    private val keyUserId = "key_user_id"
    private val keyUserEmail = "key_user_email"

    // Use regular SharedPreferences to avoid adding a new dependency in this change.
    private val sharedPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun saveCurrentUserId(userId: String) {
        sharedPrefs.edit().putString(keyUserId, userId).apply()
    }

    fun getCurrentUserId(): String? {
        return sharedPrefs.getString(keyUserId, null)
    }
    
    fun saveUserEmail(email: String) {
        sharedPrefs.edit().putString(keyUserEmail, email).apply()
    }
    
    fun getUserEmail(): String? {
        return sharedPrefs.getString(keyUserEmail, null)
    }

    fun clearSession() {
        sharedPrefs.edit().remove(keyUserId).remove(keyUserEmail).apply()
    }
}
