package com.example.recapme.data.api

data class RecapRequest(
    val conversation: String,
    val style: String
)

data class RecapResponse(
    val Title: String,
    val Users: List<String>,
    val Recap: String
)