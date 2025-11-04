package com.example.noteapp.model

import java.io.Serializable

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
) : Serializable
