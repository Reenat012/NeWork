package com.example.nework2.repository

import androidx.paging.PagingData
import com.example.nework2.dto.Event
import com.example.nework2.dto.FeedItem
import com.example.nework2.model.AttachmentModel
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    val dataEvent: Flow<PagingData<FeedItem>>

    suspend fun saveEvent(event: Event)
    suspend fun saveEventWithAttachment(event: Event, attachmentModel: AttachmentModel)
    suspend fun deleteEvent(id: Long)
    suspend fun likeEvent(event: Event)
}