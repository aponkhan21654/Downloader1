package com.example.data.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface CobaltApiService {
    @POST
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun downloadMedia(
        @Url apiEndpoint: String,
        @Body request: CobaltRequest
    ): CobaltResponse
}
