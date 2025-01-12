package com.example.nework2.viewmodel

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.example.nework2.auth.AppAuth
import com.example.nework2.dto.Coordinates
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.Post
import com.example.nework2.enumeration.AttachmentType
import com.example.nework2.model.AttachmentModel
import com.example.nework2.model.InvolvedItemModel
import com.example.nework2.model.InvolvedItemType
import com.example.nework2.repository.PostRepository
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.time.OffsetDateTime
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
val emptyPost = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = null,
    authorAvatar = null,
    content = "",
    published = OffsetDateTime.now(),
    coords = null,
    link = null,
    mentionIds = emptyList(),
    mentionedMe = false,
    likeOwnerIds = emptyList(),
    likedByMe = false,
    attachment = null,
    users = mapOf(),
    ownedByMe = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

    val data: Flow<PagingData<FeedItem>> = appAuth.authState
        .flatMapLatest { auth ->
            postRepository.data.map {
                it.map { feedItem ->
                    if (feedItem is Post) {
                        feedItem.copy(
                            ownedByMe = auth.id == feedItem.authorId,
                            likedByMe = !feedItem.likeOwnerIds.none { id ->
                                id == auth.id
                            }
                        )
                    } else {
                        feedItem
                    }
                }
            }
        }.flowOn(Dispatchers.Default)


    @RequiresApi(Build.VERSION_CODES.O)
    private val _editedPost = MutableLiveData(emptyPost)
    @RequiresApi(Build.VERSION_CODES.O)
    val editedPost: LiveData<Post> = _editedPost

    val postData = MutableLiveData<Post>()

    val involvedData = MutableLiveData(InvolvedItemModel())

    private val _attachmentData: MutableLiveData<AttachmentModel?> = MutableLiveData(null)
    val attachmentData: LiveData<AttachmentModel?>
        get() = _attachmentData


    @RequiresApi(Build.VERSION_CODES.O)
    fun savePost(content: String) {
        val text = content.trim()
        if (_editedPost.value?.content == text) {
            _editedPost.value = emptyPost
            return
        }
        _editedPost.value = _editedPost.value?.copy(content = text)
        _editedPost.value?.let {
            viewModelScope.launch {
                val attachment = _attachmentData.value
                if (attachment == null) {
                    postRepository.savePost(
                        it
                    )
                } else {
                    postRepository.savePostWithAttachment(
                        it, attachment
                    )
                }
            }
        }
        _editedPost.value = emptyPost
        _attachmentData.value = null
    }

    fun deletePost(post: Post) = viewModelScope.launch {
        postRepository.deletePost(post.id)
    }

    fun like(post: Post) = viewModelScope.launch {
        try {
            postRepository.like(post)
        } catch (e: Exception) {
            println(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun edit(post: Post) {
        _editedPost.value = post
    }

    fun setAttachment(uri: Uri, file: File, attachmentType: AttachmentType) {
        _attachmentData.value = AttachmentModel(attachmentType, uri, file)
    }

    fun removePhoto() {
        _attachmentData.value = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setCoord(point: Point?) {
        if (point != null) {
            _editedPost.value = _editedPost.value?.copy(
                coords = Coordinates(point.latitude, point.longitude)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun removeCoords() {
        _editedPost.value = _editedPost.value?.copy(
            coords = null
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setMentionId(selectedUsers: List<Long>) {
        _editedPost.value = _editedPost.value?.copy(
            mentionIds = selectedUsers
        )
    }

    fun openPost(post: Post) {
        postData.value = post
    }


    suspend fun getInvolved(involved: List<Long>, involvedItemType: InvolvedItemType) {
        val list = involved
            .let {
                if (it.size > 4) it.take(5) else it
            }
            .map {
                viewModelScope.async { postRepository.getUser(it) }
            }.awaitAll()

        synchronized(involvedData) {
            when (involvedItemType) {

                InvolvedItemType.LIKERS -> {
                    involvedData.value = involvedData.value?.copy(
                        likers = list
                    )
                }

                InvolvedItemType.MENTIONED -> {
                    involvedData.value = involvedData.value?.copy(
                        mentioned = list
                    )
                }

                else -> return
            }
        }

    }

    fun resetInvolved() {
        involvedData.value = InvolvedItemModel()
    }

}