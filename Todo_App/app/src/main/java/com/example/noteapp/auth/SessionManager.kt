package com.example.noteapp.auth

import android.content.Context

class SessionManager(context: Context) {
    private val prefsName = "session_prefs"
    private val keyUserId = "key_user_id"
    private val keyUserEmail = "key_user_email"
    private val keyUserName = "key_user_name"

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
    
    fun saveUserName(name: String) {
        sharedPrefs.edit().putString(keyUserName, name).apply()
    }
    
    fun getUserName(): String? {
        return sharedPrefs.getString(keyUserName, null)
    }

    fun clearSession() {
        sharedPrefs.edit()
            .remove(keyUserId)
            .remove(keyUserEmail)
            .remove(keyUserName)
            .apply()
    }
}
