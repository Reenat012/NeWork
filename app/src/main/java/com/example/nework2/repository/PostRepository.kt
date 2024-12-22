package com.example.nework2.repository

import androidx.paging.PagingData
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Post
import com.example.nework2.model.ModelPhoto
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    //подписка на посты
    val data: Flow<PagingData<FeedItem>>
    fun repost(id: Long)
    suspend fun getAll()
    fun getNewer(newerId: Long) : Flow<Int>
    fun getAllVisible()
    suspend fun getHiddenCount() : Flow<Int>
    suspend fun changeHiddenPosts()
    suspend fun likeByIdAsync(id: Long) : Post
    suspend fun disLikeByIdAsync(id: Long) : Post
    //  fun removeById(id: Long)
    suspend fun removeByIdAsync(id: Long)
    //    fun save(post: Post): Post
    suspend fun saveAsync(post: Post) : Post
    suspend fun saveWithAttachment(post: Post, photo: ModelPhoto)
//    fun openPostById(id: Long): Post
}