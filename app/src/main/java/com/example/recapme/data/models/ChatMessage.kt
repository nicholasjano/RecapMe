package com.example.recapme.data.models

data class ChatMessage(
    val timestamp: Long,
    val sender: String,
    val content: String
)