package com.nicholasjano.recapme.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RecapApiService {
    @POST("api/recap")
    suspend fun generateRecap(@Body request: RecapRequest): Response<RecapResponse>
}