package com.example.nework2.repositoryImpl

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.nework2.R
import com.example.nework2.api.EventApiService
import com.example.nework2.api.JobApiService
import com.example.nework2.api.PostApiService
import com.example.nework2.api.UserApiService
import com.example.nework2.auth.AppAuth
import com.example.nework2.dao.EventDao
import com.example.nework2.dao.PostDao
import com.example.nework2.dao.UserDao
import com.example.nework2.dto.Attachment
import com.example.nework2.dto.Event
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Job
import com.example.nework2.dto.Media
import com.example.nework2.dto.Post
import com.example.nework2.dto.UserResponse
import com.example.nework2.entity.EventEntity
import com.example.nework2.entity.PostEntity
import com.example.nework2.entity.UserEntity
import com.example.nework2.error.ApiError
import com.example.nework2.error.NetworkError
import com.example.nework2.model.AttachmentModel
import com.example.nework2.model.AuthModel
import com.example.nework2.repository.EventRemoteMediator
import com.example.nework2.repository.PostRemoteMediator
import com.example.nework2.repository.Repository
import com.example.nework2.repository.UserRemoteMediator
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class RepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val appAuth: AppAuth,
    private val userApiService: UserApiService,
    private val jobApiService: JobApiService,
    private val eventApiService: EventApiService,
    private val postApiService: PostApiService,
    private val postDao: PostDao,
    postRemoteMediator: PostRemoteMediator,
    private val eventDao: EventDao,
    eventRemoteMediator: EventRemoteMediator,
    userDao: UserDao,
    userRemoteMediator: UserRemoteMediator
) : Repository {

    val dataAuth: StateFlow<AuthModel> = appAuth.authState

    val dataPost: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 4, enablePlaceholders = false),
        pagingSourceFactory = { postDao.pagingSource() },
        remoteMediator = postRemoteMediator
    ).flow
        .map {
            it.map(PostEntity::toDto)
        }


    val dataEvent: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 4, enablePlaceholders = false),
        pagingSourceFactory = { eventDao.pagingSource() },
        remoteMediator = eventRemoteMediator
    ).flow
        .map {
            it.map(EventEntity::toDto)
        }

    val dataUsers: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 4, enablePlaceholders = false),
        pagingSourceFactory = { userDao.pagingSource() },
        remoteMediator = userRemoteMediator
    ).flow.map {
        it.map(UserEntity::toDto)
    }

    private val _dataJob = MutableLiveData<List<Job>>()
    val dataJob: LiveData<List<Job>> = _dataJob

    override suspend fun register(
        login: String,
        name: String,
        pass: String,
        attachmentModel: AttachmentModel?
    ) {
        try {
            val response = if (attachmentModel != null) {
                val part = MultipartBody.Part.createFormData(
                    "file",
                    attachmentModel.file.name,
                    attachmentModel.file.asRequestBody()
                )
                userApiService.usersRegistrationWithPhoto(login, pass, name, part)
            } else {
                userApiService.usersRegistration(login, pass, name)
            }

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> {
                        toastMsg(context.getString(R.string.the_user_is_already_registered))
                    }

                    415 -> {
                        toastMsg(context.getString(R.string.incorrect_photo_format))
                    }

                    else -> throw ApiError(response.code(), response.message())
                }
                return
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appAuth.setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw com.example.nework2.error.UnknownError
        }
    }

    override suspend fun login(login: String, pass: String) {
        try {
            val response = userApiService.usersAuthentication(login, pass)
            if (!response.isSuccessful) {
                when (response.code()) {
                    400 -> {
                        toastMsg(context.getString(R.string.incorrect_password))
                    }

                    404 -> {
                        toastMsg(context.getString(R.string.user_unregistered))
                    }

                    else -> throw ApiError(response.code(), response.message())
                }
                return
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appAuth.setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw com.example.nework2.error.UnknownError
        }
    }

    override fun logout() {
        appAuth.removeAuth()
    }

    private suspend fun saveMedia(file: File): Response<Media> {
        val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        return eventApiService.mediaSaveMedia(part)
    }

    override suspend fun saveEvent(event: Event) {
        try {
            val response = eventApiService.eventsSaveEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw com.example.nework2.error.UnknownError
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

            val response = eventApiService.eventsSaveEvent(
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
            throw com.example.nework2.error.UnknownError
        }
    }

    override suspend fun deleteEvent(id: Long) {
        try {
            val response = eventApiService.eventsDeleteEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            eventDao.deleteEvent(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw com.example.nework2.error.UnknownError
        }
    }

    override suspend fun likeEvent(event: Event) {
        try {
            val response = when (event.likedByMe) {
                true -> {
                    eventApiService.eventsUnLikeEvent(event.id)
                }

                else -> {
                    eventApiService.eventsLikeEvent(event.id)
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
            throw com.example.nework2.error.UnknownError
        }
    }

    override suspend fun getMyJobs() {
        try {
            val response = jobApiService.myJobGetAllJob()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            _dataJob.value = body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw com.example.nework2.error.UnknownError
        }
    }

    override suspend fun getJobs(userId: Long) {
        try {
            val response = jobApiService.jobsGetAllJob(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            _dataJob.value = body
        } catch (e: Exception) {
            if (e is IOException) {
                throw NetworkError
            } else if (e is ApiError) {
                throw Error("ApiError")
            }
            else if (e is JsonSyntaxException) {
                // Логируем ошибку и обрабатываем некорректный JSON
                Log.e("JSON Error", "Error parsing JSON: ${e.message}")
                // Проверяем, что именно вызвало исключение
                if (e.cause != null && e.cause is IllegalStateException) {
                    val illegalStateException = e.cause as IllegalStateException
                    if (illegalStateException.message == "Expected a string but was NULL") {
                        // Обрабатываем случай, когда ожидалась строка, но было null
                        // Здесь можно добавить дополнительную обработку ошибки
                        Log.e("JSON Error", "String expected but was null at line 1 column 482 path $[3].finish")
                    }}
            else {
                throw com.example.nework2.error.UnknownError
            }
        }
    }}

    override suspend fun saveJob(job: Job) {
        try {
            val response = jobApiService.myJobSaveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())

            getMyJobs()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw com.example.nework2.error.UnknownError
        }
    }

    override suspend fun deleteJob(id: Long) {
        try {
            val response = jobApiService.myJobDeleteJob(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            _dataJob.value = _dataJob.value?.filter { it.id != id }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw com.example.nework2.error.UnknownError
        }
    }


    private fun toastMsg(msg: String) {
        Toast.makeText(
            context,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }

}