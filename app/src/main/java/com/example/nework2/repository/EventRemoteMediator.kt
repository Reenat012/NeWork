package com.example.nework2.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.nework2.api.EventApiService
import com.example.nework2.dao.EventDao
import com.example.nework2.dao.EventRemoteKeyDao
import com.example.nework2.db.AppDb
import com.example.nework2.entity.EventEntity
import com.example.nework2.entity.EventRemoteKeyEntity
import com.example.nework2.entity.PostRemoteKeyEntity
import com.example.nework2.entity.toEntity
import com.example.nework2.error.ApiError
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class EventRemoteMediator @Inject constructor(
    private val apiService: EventApiService,
    private val appDb: AppDb,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
) : RemoteMediator<Int, EventEntity>() {

    override suspend fun initialize(): InitializeAction =
        if (eventDao.isEmpty()) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {

        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    val id = eventRemoteKeyDao.max()
                    if (id != null) {
                        apiService.eventsGetAfterEvent(id, state.config.pageSize)
                    } else {
                        apiService.eventsGetLatestPageEvent(state.config.pageSize)
                    }
                }

                LoadType.PREPEND -> {
                    val id = eventRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    apiService.eventsGetAfterEvent(id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.eventsGetBeforeEvent(id, state.config.pageSize)
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
                            if (eventDao.isEmpty()) {
                                eventRemoteKeyDao.insert(
                                    listOf(
                                        EventRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.AFTER,
                                            body.first().id
                                        ),
                                        EventRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.BEFORE,
                                            body.last().id
                                        )
                                    )
                                )
                            } else {
                                eventRemoteKeyDao.insert(
                                    EventRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    )
                                )
                            }
                        }

                        LoadType.PREPEND -> {
                            eventRemoteKeyDao.insert(
                                EventRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        }

                        LoadType.APPEND -> {
                            eventRemoteKeyDao.insert(
                                EventRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                    }

                    eventDao.insertAll(body.toEntity())
                }
            }

            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}