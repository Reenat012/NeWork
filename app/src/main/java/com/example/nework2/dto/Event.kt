package com.example.nework2.dto

import java.time.OffsetDateTime

data class Event(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: OffsetDateTime,
    val published: OffsetDateTime,
    val coords: Coordinates? = null,
    val type: EventType,
    val likeOwnerIds: List<Long>,
    val likedByMe: Boolean,
    val speakerIds: List<Long>,
    val participantsIds: List<Long>,
    val participatedByMe: Boolean,
    val attachment: Attachment? = null,
    val link: String? = null,
    val users: Map<String, UserPreview>,
    val ownedByMe: Boolean = false,
) : FeedItem

enum class EventType {
    OFFLINE,
    ONLINE
}