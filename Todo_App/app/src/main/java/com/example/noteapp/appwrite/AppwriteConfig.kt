package com.example.noteapp.appwrite

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases

object AppwriteConfig {
    // Replace these with your actual Appwrite project details
    private const val ENDPOINT = "https://sgp.cloud.appwrite.io/v1"
    const val PROJECT_ID = "6908ccdf00223cfe80cd" // Replace with your project ID    
    const val DATABASE_ID = "6908cde40006b4bbd549" // Replace with your database ID
    
    // ⚠️ IMPORTANT: These should be the actual Collection IDs from your Appwrite console
    // Not collection names. Collection IDs look like: "6908cde40006b4bbd549"
    // Go to your Appwrite console -> Database -> Collections and copy the actual IDs
    const val USER_COLLECTION_ID = "users" // Replace with actual collection ID
    const val NOTE_COLLECTION_ID = "notes" // Replace with actual collection ID  
    const val TODO_COLLECTION_ID = "todos" // Replace with actual collection ID
    const val WORKSPACE_COLLECTION_ID = "workspaces" // Replace with actual collection ID
    const val WORKSPACE_MEMBER_COLLECTION_ID = "workspace_members" // Replace with actual collection ID
    const val WORKSPACE_INVITATION_COLLECTION_ID = "workspace_invitations" // Replace with actual collection ID

    lateinit var client: Client
    lateinit var account: Account
    lateinit var databases: Databases
    
    fun init(context: Context) {
        client = Client(context)
            .setEndpoint(ENDPOINT)
            .setProject(PROJECT_ID)

        account = Account(client)
        databases = Databases(client)
    }
    
    // Legacy methods for backward compatibility
    fun getClient(context: Context): Client {
        if (!::client.isInitialized) {
            init(context)
        }
        return client
    }
    
    fun getAccount(context: Context): Account {
        if (!::account.isInitialized) {
            init(context)
        }
        return account
    }
    
    fun getDatabases(context: Context): Databases {
        if (!::databases.isInitialized) {
            init(context)
        }
        return databases
    }
}
