package com.example.nework2.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.nework2.api.PostApiService
import com.example.nework2.dao.PostDao
import com.example.nework2.dao.PostRemoteKeyDao
import com.example.nework2.db.AppDb
import com.example.nework2.entity.PostEntity
import com.example.nework2.entity.PostRemoteKeyEntity
import com.example.nework2.entity.toEntity
import com.example.nework2.error.ApiError
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class PostRemoteMediator @Inject constructor(
    private val apiService: PostApiService,
    private val appDb: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun initialize(): InitializeAction =
        if (postDao.isEmpty()) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {

        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    val id = postRemoteKeyDao.max()
                    if (id != null) {
                        apiService.postsGetAfterPost(id, state.config.pageSize)
                    } else {
                        apiService.postsGetLatestPage(state.config.pageSize)
                    }
                }

                LoadType.PREPEND -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    apiService.postsGetAfterPost(id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.postsGetBeforePost(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body()
                ?: throw ApiError(response.code(), response.message())

            if (body.isNotEmpty()) {
                appDb.withTransaction {
                    when (loadType) {
                        LoadType.REFRESH -> {
                            if (postDao.isEmpty()) {
                                postRemoteKeyDao.insert(
                                    listOf(
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.AFTER,
                                            body.first().id
                                        ),
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.BEFORE,
                                            body.last().id
                                        )
                                    )
                                )
                            } else {
                                postRemoteKeyDao.insert(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    )
                                )
                            }
                        }

                        LoadType.PREPEND -> {
                            println("prep")
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        }

                        LoadType.APPEND -> {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                    }

                    postDao.insertAll(body.toEntity())

                }
            }

            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}