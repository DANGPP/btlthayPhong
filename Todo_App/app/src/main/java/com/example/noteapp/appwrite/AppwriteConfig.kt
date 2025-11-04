package com.example.noteapp.appwrite

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases

object AppwriteConfig {
    // Replace these with your actual Appwrite project details
    private const val ENDPOINT = "https://sgp.cloud.appwrite.io/v1"
    private const val PROJECT_ID = "6908ccdf00223cfe80cd" // Replace with your project ID
    const val DATABASE_ID = "6908cde40006b4bbd549" // Replace with your database ID
    const val USER_COLLECTION_ID = "users" // Collection ID for users
    const val NOTE_COLLECTION_ID = "notes" // Collection ID for notes
    const val TODO_COLLECTION_ID = "todos" // Collection ID for todos

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
