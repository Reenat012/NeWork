package com.example.nework2.dto

import com.example.nework2.enumeration.AttachmentType
import java.time.OffsetDateTime


sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val published: OffsetDateTime,
    val coords: Coordinates? = null,
    val link: String? = null,
    val mentionIds: List<Long>,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Long>,
    val likedByMe: Boolean,
    val attachment: Attachment? = null,
    val users: Map<String, UserPreview>,
    val ownedByMe: Boolean = false,
) : FeedItem


