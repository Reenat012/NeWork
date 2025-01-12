package com.example.nework2.repository

import androidx.paging.PagingData
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Post
import com.example.nework2.dto.UserResponse
import com.example.nework2.model.AttachmentModel
import com.example.nework2.model.ModelPhoto
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    //подписка на посты
    val data: Flow<PagingData<FeedItem>>

    suspend fun getUser(id: Long): UserResponse
    suspend fun like(post: Post)
    suspend fun savePost(post: Post)
    suspend fun savePostWithAttachment(post: Post, attachmentModel: AttachmentModel)
    suspend fun deletePost(id: Long)
}