package com.nicholasjano.recapme.data.models

data class ChatMessage(
    val timestamp: Long,
    val sender: String,
    val content: String
)