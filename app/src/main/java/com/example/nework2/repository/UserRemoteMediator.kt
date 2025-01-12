package com.example.nework2.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.nework2.api.UserApiService
import com.example.nework2.dao.UserDao
import com.example.nework2.entity.UserEntity
import com.example.nework2.entity.toEntity
import com.example.nework2.error.ApiError
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class UserRemoteMediator @Inject constructor(
    private val apiService: UserApiService,
    private val userDao: UserDao
) : RemoteMediator<Int, UserEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UserEntity>
    ): MediatorResult {
        return try {
            if (loadType == LoadType.REFRESH) {
                val response = apiService.usersGetAllUser()

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }

                val body = response.body() ?: throw ApiError(response.code(), response.message())

                userDao.insertAll(body.toEntity())
            }
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }

    }
}