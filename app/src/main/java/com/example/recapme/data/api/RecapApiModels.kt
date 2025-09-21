package com.example.recapme.data.api

data class RecapRequest(
    val conversation: String,
    val style: String
)

data class RecapResponse(
    val title: String,
    val users: List<String>,
    val recap: String
)