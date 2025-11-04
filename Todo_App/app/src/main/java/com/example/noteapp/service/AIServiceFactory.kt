package com.example.noteapp.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class AIServiceFactory(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("ai_config", Context.MODE_PRIVATE)
    
    fun createAIService(): AIService? {
        val apiKey = prefs.getString("gemini_api_key", null)
        Log.d("AIServiceFactory", "Retrieved API Key: $apiKey")
        return if (apiKey.isNullOrBlank()) null else GeminiService(apiKey)
    }
    
    fun setGeminiKey(apiKey: String) {
        prefs.edit()
            .putString("gemini_api_key", apiKey)
            .apply()
    }
    
    fun hasValidApiKey(): Boolean {
        return !prefs.getString("gemini_api_key", null).isNullOrBlank()
    }
    
    fun getCurrentProvider(): String {
        return "Gemini"
    }
}
