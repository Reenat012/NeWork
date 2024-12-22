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
import com.example.nework2.error.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    val count = postDao.count()
                    val maxId = postRemoteKeyDao.max() ?: 0//return MediatorResult.Success(false)
                    if (count == 0) {
                        //при пустой базе данных стоит отправлять запрос getLatest
                        apiService.getLatest(state.config.pageSize)
                    } else {
                        //в остальных случаях getAfter
                        apiService.getAfter(maxId, state.config.pageSize)

//                        return MediatorResult.Success(false)
                    }

//                    apiService.getLatest(state.config.pageSize)
                }

                //скролл вниз
                //postRemoteKeyDao.min() загружает самый старый пост из БД
                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
                //когда пользовател скроллит вверх, у него не будет загружаться новая страница
                ////postRemoteKeyDao.min() загружает самый новый пост из БД
                LoadType.PREPEND -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    apiService.getAfter(id, state.config.pageSize)
                }
            }

            if (!result.isSuccessful) {
                throw ApiError(result.code(), result.message())
            }

            val data = result.body().orEmpty()

            appDb.withTransaction {
                //заполняем таблицу ключей данными, которые приходят по сети
                //для этого узнаем какой был тип входных данных
                when (loadType) {
                    LoadType.REFRESH -> {
                        //очищаем таблицу с постами
                        //по условию дз очищать таблицу не нужно
                        postDao.clear()

                        val count = postDao.count()

                        if (count == 0) {
                            // Обновляем ключ BEFORE до текущего минимального значения
                            postRemoteKeyDao.insert(
                                listOf(
                                    PostRemoteKeyEntity(
                                        //берем самый первый пост из пришедшего списка
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        data.first().id
                                    ),
                                    PostRemoteKeyEntity(
                                        //берем самый первый пост из пришедшего списка
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        data.last().id
                                    )
                                )
                            )
                        } else {
                            // Если база данных не пуста, не обновляем ключ BEFORE
                            //записываем только ключ after
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    //берем самый первый пост из пришедшего списка
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    data.first().id
                                )
                            )
                        }
                    }

                    LoadType.PREPEND -> {
                        //скролл вверх
                        //записываем только ключ after
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                //берем самый первый пост из пришедшего списка
                                PostRemoteKeyEntity.KeyType.AFTER,
                                data.first().id
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        //скролл вниз
                        //записываем только ключ before
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                //берем самый первый пост из пришедшего списка
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                data.last().id
                            )
                        )
                    }
                }

                postDao.insert(data.map { PostEntity.fromDto(it) })

            }

            return MediatorResult.Success(data.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}