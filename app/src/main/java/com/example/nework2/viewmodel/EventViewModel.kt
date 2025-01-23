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
import com.example.nework2.dto.Event
import com.example.nework2.dto.EventType
import com.example.nework2.dto.FeedItem
import com.example.nework2.enumeration.AttachmentType
import com.example.nework2.model.AttachmentModel
import com.example.nework2.model.InvolvedItemModel
import com.example.nework2.model.InvolvedItemType
import com.example.nework2.repository.AuthRepository
import com.example.nework2.repository.EventRepository
import com.example.nework2.repository.Repository
import com.example.nework2.repository.UserRepository
import com.example.nework2.repositoryImpl.PostRepositoryImpl
import com.example.nework2.repositoryImpl.RepositoryImpl
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

val emptyEvent = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = null,
    authorAvatar = null,
    content = "",
    datetime = OffsetDateTime.now(),
    published = OffsetDateTime.now(),
    coords = null,
    type = EventType.ONLINE,
    likeOwnerIds = listOf(),
    likedByMe = false,
    speakerIds = listOf(),
    participantsIds = listOf(),
    participatedByMe = false,
    attachment = null,
    link = null,
    users = mapOf(),
    ownedByMe = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: RepositoryImpl,
    private val userRepository: RepositoryImpl,
    private val postRepositoryImpl: PostRepositoryImpl,
    appAuth: AppAuth
) : ViewModel() {

    val data: Flow<PagingData<FeedItem>> = appAuth.authState
        .flatMapLatest { auth ->
            eventRepository.dataEvent.map {
                it.map { feedItem ->
                    if (feedItem is Event) {
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

    val eventData = MutableLiveData<Event>()

    val involvedData = MutableLiveData(InvolvedItemModel())

    private val _editedEvent = MutableLiveData(emptyEvent)
    val editedEvent: LiveData<Event> = _editedEvent

    private val _attachmentData: MutableLiveData<AttachmentModel?> = MutableLiveData(null)

    fun saveEvent(content: String) {
        val text = content.trim()
        if (_editedEvent.value?.content == text) {
            _editedEvent.value = emptyEvent
            return
        }
        _editedEvent.value = _editedEvent.value?.copy(content = text)
        _editedEvent.value?.let {
            viewModelScope.launch {
                val attachment = _attachmentData.value
                if (attachment == null) {
                    eventRepository.saveEvent(it)
                } else {
                    eventRepository.saveEventWithAttachment(
                        it, attachment
                    )
                }
            }
        }
        _editedEvent.value = emptyEvent
        _attachmentData.value = null
    }


    val attachmentData: LiveData<AttachmentModel?>
        get() = _attachmentData

    fun setAttachment(uri: Uri, file: File, attachmentType: AttachmentType) {
        _attachmentData.value = AttachmentModel(attachmentType, uri, file)
    }

    fun removeAttachment() {
        _attachmentData.value = null
    }

    fun deleteEvent(event: Event) = viewModelScope.launch {
        eventRepository.deleteEvent(event.id)
    }

    fun setCoord(point: Point?) {
        if (point != null) {
            _editedEvent.value = _editedEvent.value?.copy(
                coords = Coordinates(point.latitude, point.longitude)
            )
        }
    }

    fun removeCoords() {
        _editedEvent.value = _editedEvent.value?.copy(
            coords = null
        )
    }

    fun setMentionId(selectedUsers: List<Long>) {
        _editedEvent.value = _editedEvent.value?.copy(
            speakerIds = selectedUsers
        )
    }

    fun like(event: Event) = viewModelScope.launch {
        eventRepository.likeEvent(event)
    }

    fun edit(event: Event) {
        _editedEvent.value = event
    }

    fun setDateTime(date: OffsetDateTime) {
        _editedEvent.value = _editedEvent.value?.copy(
            datetime = date
        )
        println(_editedEvent.value)
    }

    fun setEventType(eventType: EventType) {
        _editedEvent.value = _editedEvent.value?.copy(
            type = eventType
        )
    }

    fun openEvent(event: Event) {
        eventData.value = event
    }

    suspend fun getInvolved(involved: List<Long>, involvedItemType: InvolvedItemType) {
        val list = involved
            .let {
                if (it.size > 4) it.take(5) else it
            }
            .map {
                viewModelScope.async { postRepositoryImpl.getUser(it) }
            }.awaitAll()

        synchronized(involvedData) {
            when (involvedItemType) {
                InvolvedItemType.SPEAKERS -> {
                    involvedData.value = involvedData.value?.copy(
                        speakers = list
                    )
                }

                InvolvedItemType.LIKERS -> {
                    involvedData.value = involvedData.value?.copy(
                        likers = list
                    )
                }

                InvolvedItemType.PARTICIPANT -> {
                    involvedData.value = involvedData.value?.copy(
                        participants = list
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

