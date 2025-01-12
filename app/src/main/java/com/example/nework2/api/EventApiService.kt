package com.example.nework2.api

import com.example.nework2.dto.Event
import com.example.nework2.dto.Media
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApiService {
    @GET("api/events")
    suspend fun eventsGetAllEvent(): Response<List<Event>>

    @POST("api/events")
    suspend fun eventsSaveEvent(
        @Body event: Event
    ): Response<Event>

    @Multipart
    @POST("api/media")
    suspend fun mediaSaveMedia(
        @Part file: MultipartBody.Part
    ): Response<Media>

    @POST("api/events/{id}/participants")
    suspend fun eventsSaveParticipantsEvent(
        @Path("id") id: Long,
    ): Response<Event>

    @DELETE("api/events/{id}/participants")
    suspend fun eventsDeleteParticipantsEvent(
        @Path("id") id: Long,
    ): Response<Event>

    @POST("api/events/{id}/likes")
    suspend fun eventsLikeEvent(
        @Path("id") id: Long,
    ): Response<Event>

    @DELETE("api/events/{id}/likes")
    suspend fun eventsUnLikeEvent(
        @Path("id") id: Long,
    ): Response<Event>

    @GET("api/events/{id}/newer")
    suspend fun eventsGetNewerEvent(
        @Path("id") id: Long,
    ): Response<List<Event>>

    @GET("api/events/{id}/before")
    suspend fun eventsGetBeforeEvent(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("api/events/{id}/after")
    suspend fun eventsGetAfterEvent(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("api/events/{id}")
    suspend fun eventsGetEvent(
        @Path("id") id: Long,
    ): Response<Event>

    @DELETE("api/events/{id}")
    suspend fun eventsDeleteEvent(
        @Path("id") id: Long,
    ): Response<Unit>

    @GET("api/events/latest")
    suspend fun eventsGetLatestPageEvent(@Query("count") count: Int): Response<List<Event>>

}