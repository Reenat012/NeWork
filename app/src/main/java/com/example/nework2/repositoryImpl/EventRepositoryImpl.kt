package com.example.nework2.repositoryImpl

import android.annotation.SuppressLint
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.nework2.api.EventApiService
import com.example.nework2.dao.EventDao
import com.example.nework2.dto.Attachment
import com.example.nework2.dto.Event
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Media
import com.example.nework2.entity.EventEntity
import com.example.nework2.error.ApiError
import com.example.nework2.error.NetworkError
import com.example.nework2.error.UnknownError
import com.example.nework2.model.AttachmentModel
import com.example.nework2.repository.EventRemoteMediator
import com.example.nework2.repository.EventRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject
import androidx.paging.map

class EventRepositoryImpl @OptIn(ExperimentalPagingApi::class)
@Inject constructor(
    @ApplicationContext
    eventRemoteMediator: EventRemoteMediator,
    private val apiService: EventApiService,
    private val eventDao: EventDao,
    @SuppressLint("NewApi") override val dataEvent: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 4, enablePlaceholders = false),
        pagingSourceFactory = { eventDao.pagingSource() },
        remoteMediator = eventRemoteMediator
    ).flow
        .map {
            it.map(EventEntity::toDto)
        }

    ) : EventRepository {
    override suspend fun saveEvent(event: Event) {
        try {
            val response = apiService.eventsSaveEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEventWithAttachment(event: Event, attachmentModel: AttachmentModel) {
        try {
            val mediaResponse = saveMedia(attachmentModel.file)
            if (!mediaResponse.isSuccessful) {
                throw ApiError(mediaResponse.code(), mediaResponse.message())
            }
            val media = mediaResponse.body() ?: throw ApiError(
                mediaResponse.code(),
                mediaResponse.message()
            )

            val response = apiService.eventsSaveEvent(
                event.copy(
                    attachment = Attachment(
                        media.id,
                        attachmentModel.attachmentType
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun deleteEvent(id: Long) {
        try {
            val response = apiService.eventsDeleteEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            eventDao.deleteEvent(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            UnknownError
        }
    }

    override suspend fun likeEvent(event: Event) {
        try {
            val response = when (event.likedByMe) {
                true -> {
                    apiService.eventsUnLikeEvent(event.id)
                }

                else -> {
                    apiService.eventsLikeEvent(event.id)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun saveMedia(file: File): Response<Media> {
        val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        return apiService.mediaSaveMedia(part)
    }
}