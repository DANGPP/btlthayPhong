package com.example.noteapp.model

import java.io.Serializable

data class Note(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var createdTime: String = "",
    var userId: String = "" // Link to the authenticated user
) : Serializable {
    
    // Constructor for creating new notes (without ID)
    constructor(
        title: String,
        description: String,
        createdTime: String,
        userId: String
    ) : this("", title, description, createdTime, userId)
}

