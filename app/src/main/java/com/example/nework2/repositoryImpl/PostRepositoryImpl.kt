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
import com.example.nework2.dto.Ad
import com.example.nework2.dto.Attachment
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Media
import com.example.nework2.dto.Post
import com.example.nework2.entity.PostEntity
import com.example.nework2.entity.toEntity
import com.example.nework2.enumeration.AttachmentType
import com.example.nework2.error.ApiError
import com.example.nework2.error.NetworkError
import com.example.nework2.model.ModelPhoto
import com.example.nework2.repository.PostRemoteMediator
import com.example.nework2.repository.PostRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

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
            postDao.getPagingSourse()
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
                //реализуем вставку элементов с рекламой
                //insertSeparators последовательно обходит элементы от previous (предыдущий элемент) до next(следующий элемент)
                .insertSeparators { previous, next ->
                    //через каждые 5 элементов впихиваем рекламу
                    if (previous?.id?.rem(5)?.toInt() == 0) {
                        Ad(Random.nextLong(), "figma.jpg")
                    } else null
                }
        }
//        postDao.getAllVisible().map { it.map(PostEntity::toDto) }

    override fun repost(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            //если что-то пошло не так
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            //если все хорошо
            val posts = response.body() ?: throw RuntimeException("Response body is null")

            val entities = posts.map {
                PostEntity.fromDto(it)
            }

            //записываем posts в базу данных
            postDao.insert(entities)
        } catch (e: IOException) {
            throw NetworkError
        }
//        catch (e: Exception) {
//            throw UnknownError
//        }
    }

    override fun getNewer(newerId: Long): Flow<Int> = flow {
        while (true) {
            //замедляем процесс на 10 секунд
            delay(10.seconds)

            try {//получаем вновь сгенерированные посты
                val postsResponse = apiService.getNewer(newerId)

                //пробуем взять тело постов
                val posts = postsResponse.body().orEmpty()

                //выбрасываем количество новых сообщений
                emit(posts.size)

                //вписываем сгенирированный список постов в локальную  БД
                //полученные посты невидимы, когда записаны в базу
                postDao.insert(posts.toEntity(hidden = true))

            } //при попытке отменить flow
            catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                //ignore
            }
        }
    }

    override fun getAllVisible() {
        //если ответ от сервера приходит не 0, значит появились новые посты
        //получаем посты только с локальной БД
        postDao.getPosts()
    }

    override suspend fun getHiddenCount(): Flow<Int> {
        //получаем количество скрытых постов
        return postDao.getHiddenCount()
    }

    override suspend fun changeHiddenPosts() {
        postDao.changeHiddenPosts()
    }

    override suspend fun likeByIdAsync(id: Long): Post {
        try {
            //модифицируем запись в локальной БД
            postDao.likeById(id)

            //отправляем запрос
            val response = apiService.likeById(id)

            //если что-то пошло не так
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            //если все хорошо
            val post = response.body() ?: throw RuntimeException("Response body is null")

            return post
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun disLikeByIdAsync(id: Long): Post {
        try {
            //модифицируем запись в локальной БД
            postDao.likeById(id)

            //отправляем запрос
            val response = apiService.dislikeById(id)

            //если что-то пошло не так
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            //если все хорошо
            val post = response.body() ?: throw RuntimeException("Response body is null")

            return post
        } catch (e: IOException) {
            postDao.likeById(id)
            throw NetworkError
        }
    }

    override suspend fun removeByIdAsync(id: Long) {
        try {
            //удаляем запись из локальной БД
            postDao.removeById(id)

            //отправляем запрос на удаление на сервер
            val response = apiService.removeById(id)

            //если что-то пошло не так
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            //сервер нам ничего не вернет, поэтому и вставлять в post нечего
        } catch (e: IOException) {
            throw NetworkError
        }
//        catch (e: Exception) {
//            throw UnknownError
//        }


//        val response = ApiService.service.removeById(id)
//        //если что-то пошло не так
//        if (!response.isSuccessful) {
//            throw RuntimeException(response.message())
//        }
//
//        //если все хорошо
//        val post = response.body() ?: throw RuntimeException("Response body is null")
//
//        postDao.insert(PostEntity.fromDto(post))
        //
    }

    override suspend fun saveAsync(post: Post): Post {
        try {
            val response = apiService.savePost(post)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))

            return body
        } catch (e: IOException) {
            throw NetworkError
        }
//        catch (e: Exception) {
//            throw UnknownError
//        }
    }

    override suspend fun saveWithAttachment(post: Post, photo: ModelPhoto) {
        //загружаем фото на сервер
        val media = upload(photo)

        //сохраняем пост с Attachment
        val postWithAttachment = try {
            post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
        } catch (e: IOException) {
            throw NetworkError
        }
//        catch (e: Exception) {
//            throw UnknownError
//        }

        //отправляем пост на сервер
        saveAsync(postWithAttachment)
    }

    private suspend fun upload(photo: ModelPhoto): Media {
        val uploadResponse = apiService.upload(
            MultipartBody.Part.createFormData("file", photo.file.name, photo.file.asRequestBody())
        )

        //проверяем на соответствие ожидаемому
        if (!uploadResponse.isSuccessful) {
            throw RuntimeException(uploadResponse.message())
        }

        //елси все хорошо, возвращаем тело запроса
        return uploadResponse.body() ?: throw ApiError(
            uploadResponse.code(),
            uploadResponse.message()
        )
    }

}