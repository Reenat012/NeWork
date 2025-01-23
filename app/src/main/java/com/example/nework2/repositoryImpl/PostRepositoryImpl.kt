package com.example.nework2.repositoryImpl

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.nework2.api.PostApiService
import com.example.nework2.dao.PostDao
import com.example.nework2.dao.PostRemoteKeyDao
import com.example.nework2.db.AppDb
import com.example.nework2.dto.Attachment
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Media
import com.example.nework2.dto.Post
import com.example.nework2.dto.UserResponse
import com.example.nework2.entity.PostEntity
import com.example.nework2.error.ApiError
import com.example.nework2.error.NetworkError
import com.example.nework2.error.UnknownError
import com.example.nework2.model.AttachmentModel
import com.example.nework2.repository.PostRemoteMediator
import com.example.nework2.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.random.Random

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: PostApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb
) : PostRepository {
    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999/"
    }

    //подписка на локальную БД с видимыми постами
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            postDao.pagingSource()
        },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb
        )
    ).flow
        .map { pagingData ->
            pagingData.map(PostEntity::toDto)
        }

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

    override suspend fun like(post: Post) {
        try {
            val response = when (post.likedByMe) {
                true -> {
                    apiService.postsUnLikePost(post.id)
                }

                else -> {
                    apiService.postsLikePost(post.id)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun savePost(post: Post) {
        try {
            val response = apiService.postsSavePost(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun savePostWithAttachment(post: Post, attachmentModel: AttachmentModel) {
        try {
            val mediaResponse = saveMedia(attachmentModel.file)
            if (!mediaResponse.isSuccessful) {
                throw ApiError(mediaResponse.code(), mediaResponse.message())
            }
            val media = mediaResponse.body() ?: throw ApiError(
                mediaResponse.code(),
                mediaResponse.message()
            )

            val response = apiService.postsSavePost(
                post.copy(
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
            postDao.insert(PostEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun deletePost(id: Long) {
        try {
            val response = apiService.postsDeletePost(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            postDao.deletePost(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw com.example.nework2.error.UnknownError
        }
    }
//        postDao.getAllVisible().map { it.map(PostEntity::toDto) }

    private suspend fun saveMedia(file: File): Response<Media> {
        val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        return apiService.mediaSaveMedia(part)
    }
}