package com.example.noteapp.appwrite

import android.content.Context
import android.util.Log
import com.example.noteapp.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppwriteTestHelper(private val context: Context) {
    private val repository = AppwriteRepository(context)
    
    companion object {
        private const val TAG = "AppwriteTestHelper"
    }
    
    fun runAllTests() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "Starting Appwrite tests...")
            
            // Test 1: Connection test
            testConnection()
            
            // Test 2: Create user
            val testUser = createTestUser()
            
            // Test 3: Get user by ID
            testUser?.let { user ->
                getUserTest(user.id)
                
                // Test 4: Update user
                updateUserTest(user.id)
                
                // Test 5: Get all users
                getAllUsersTest()
                
                // Test 6: Delete user
                deleteUserTest(user.id)
            }
            
            Log.d(TAG, "Appwrite tests completed!")
        }
    }
    
    private suspend fun testConnection() {
        Log.d(TAG, "Testing Appwrite connection...")
        val isConnected = repository.testConnection()
        if (isConnected) {
            Log.d(TAG, "✅ Connection test PASSED")
        } else {
            Log.e(TAG, "❌ Connection test FAILED")
        }
    }
    
    private suspend fun createTestUser(): User? {
        Log.d(TAG, "Testing user creation...")
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        val testUser = User(
            name = "Test User",
            email = "test@example.com",
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        val createdUser = repository.createUser(testUser)
        if (createdUser != null) {
            Log.d(TAG, "✅ User creation test PASSED - ID: ${createdUser.id}")
            return createdUser
        } else {
            Log.e(TAG, "❌ User creation test FAILED")
            return null
        }
    }
    
    private suspend fun getUserTest(userId: String) {
        Log.d(TAG, "Testing get user by ID...")
        val user = repository.getUserById(userId)
        if (user != null) {
            Log.d(TAG, "✅ Get user test PASSED - Name: ${user.name}, Email: ${user.email}")
        } else {
            Log.e(TAG, "❌ Get user test FAILED")
        }
    }
    
    private suspend fun updateUserTest(userId: String) {
        Log.d(TAG, "Testing user update...")
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        val updatedUser = User(
            id = userId,
            name = "Updated Test User",
            email = "updated@example.com",
            createdAt = "", // This won't be updated
            updatedAt = currentTime
        )
        
        val result = repository.updateUser(userId, updatedUser)
        if (result != null) {
            Log.d(TAG, "✅ User update test PASSED - New name: ${result.name}")
        } else {
            Log.e(TAG, "❌ User update test FAILED")
        }
    }
    
    private suspend fun getAllUsersTest() {
        Log.d(TAG, "Testing get all users...")
        val users = repository.getAllUsers()
        Log.d(TAG, "✅ Get all users test completed - Found ${users.size} users")
        users.forEach { user ->
            Log.d(TAG, "User: ${user.name} (${user.email})")
        }
    }
    
    private suspend fun deleteUserTest(userId: String) {
        Log.d(TAG, "Testing user deletion...")
        val isDeleted = repository.deleteUser(userId)
        if (isDeleted) {
            Log.d(TAG, "✅ User deletion test PASSED")
        } else {
            Log.e(TAG, "❌ User deletion test FAILED")
        }
    }
}
