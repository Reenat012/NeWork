package com.example.nework2.api

import com.example.nework2.dto.PushToken
import retrofit2.http.Body
import retrofit2.http.POST

interface PushApiService {
    @POST("users/push-tokens")
    suspend fun sendPushToken(@Body token: PushToken)
}

