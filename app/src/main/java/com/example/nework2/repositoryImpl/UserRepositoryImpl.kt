package com.example.nework2.repositoryImpl

import androidx.paging.PagingData
import com.example.nework2.api.UserApiService
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.UserResponse
import com.example.nework2.error.ApiError
import com.example.nework2.error.NetworkError
import com.example.nework2.error.UnknownError
import com.example.nework2.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class UserRepositoryImpl(
    override val dataUsers: Flow<PagingData<FeedItem>>,
    private val apiService: UserApiService
) : UserRepository {
    override suspend fun getUser(id: Long): UserResponse {
        try {
            val response = apiService.usersGetUser(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}