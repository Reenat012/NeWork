package com.example.nework2.api

import com.example.nework2.dto.Media
import com.example.nework2.dto.Post
import com.example.nework2.dto.UserResponse
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


interface PostApiService {
    @GET("api/users/{id}")
    suspend fun usersGetUser(
        @Path("id") id: Long,
    ): Response<UserResponse>

    @POST("api/posts/{id}/likes")
    suspend fun postsLikePost(
        @Path("id") id: Long,
    ): Response<Post>

    @DELETE("api/posts/{id}/likes")
    suspend fun postsUnLikePost(
        @Path("id") id: Long,
    ): Response<Post>

    @POST("api/posts")
    suspend fun postsSavePost(
        @Body post: Post,
    ): Response<Post>

    @Multipart
    @POST("api/media")
    suspend fun mediaSaveMedia(
        @Part file: MultipartBody.Part
    ): Response<Media>

    @GET("api/posts/{id}/newer")
    suspend fun postsGetNewerPost(@Path("id") id: Long): Response<List<Post>>

    @GET("api/posts/{id}/before")
    suspend fun postsGetBeforePost(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("api/posts/{id}/after")
    suspend fun postsGetAfterPost(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("api/post/{id}")
    suspend fun postsGetPost(
        @Path("id") id: Long,
    ): Response<Post>

    @DELETE("api/posts/{id}")
    suspend fun postsDeletePost(
        @Path("id") id: Long,
    ): Response<Unit>

    @GET("api/posts/latest")
    suspend fun postsGetLatestPage(@Query("count") count: Int): Response<List<Post>>


//    @GET("posts")
//    suspend fun getAll(): Response<List<Post>>
//
//    @GET("posts/latest")
//    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>
//
//    @GET("posts/{id}")
//    suspend fun getById(@Path("id") id: Long): Response<Post>
//
//    @GET("posts/{id}/newer")
//    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>
//
//    @GET("posts/{id}/before")
//    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>
//
//    @GET("posts/{id}/after")
//    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>
//
//    @POST("posts")
//    suspend fun savePost(@Body post: Post): Response<Post>
//
//    @DELETE("posts/{id}")
//    suspend fun removeById(@Path("id") id: Long): Response<Post>
//
//    @DELETE("posts/{id}/likes")
//    suspend fun dislikeById(@Path("id") id: Long): Response<Post>
//
//    @POST("posts/{id}/likes")
//    suspend fun likeById(@Path("id") id: Long): Response<Post>
//
//    @Multipart
//    @POST("media")
//    suspend fun upload(@Part file: MultipartBody.Part) : Response<Media>
}