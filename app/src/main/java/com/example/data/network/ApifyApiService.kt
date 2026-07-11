package com.example.data.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface ApifyApiService {
    @POST
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun downloadMedia(
        @Url apiEndpoint: String,
        @Query("token") token: String,
        @Body request: ApifyRequest
    ): List<ApifyResponseItem>
}
