package com.nicholasjano.recapme.data.api

data class RecapRequest(
    val conversation: String,
    val style: String
)

data class RecapResponse(
    val title: String,
    val participants: List<String>,
    val recap: String
)