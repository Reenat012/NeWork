package com.example.nework2.repository

import androidx.paging.PagingData
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.UserResponse
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val dataUsers: Flow<PagingData<FeedItem>>

    suspend fun getUser(id: Long): UserResponse
}