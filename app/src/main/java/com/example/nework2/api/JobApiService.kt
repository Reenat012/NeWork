package com.example.nework2.api

import com.example.nework2.dto.Job
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface JobApiService {
    @GET("api/{userId}/jobs")
    suspend fun jobsGetAllJob(
        @Path("userId") userId: Long,
    ): Response<List<Job>>

    @GET("api/my/jobs")
    suspend fun myJobGetAllJob(): Response<List<Job>>

    @POST("api/my/jobs")
    suspend fun myJobSaveJob(
        @Body job: Job
    ): Response<Job>

    @DELETE("api/my/jobs/{id}")
    suspend fun myJobDeleteJob(
        @Path("id") id: Long,
    ): Response<Unit>

}