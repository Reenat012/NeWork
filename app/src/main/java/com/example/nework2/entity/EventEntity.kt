package com.example.nework2.entity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.nework2.dto.Attachment
import com.example.nework2.dto.Coordinates
import com.example.nework2.dto.Event
import com.example.nework2.dto.EventType
import com.example.nework2.dto.UserPreview
import java.time.OffsetDateTime

@Entity(tableName = "eventEntity")
@TypeConverters(
    CoordsConverter::class,
    AttachmentConverter::class,
    MentionIdsConverter::class,
    UsersConverter::class
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
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
    val ownedByMe: Boolean = false
) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun toDto() = Event(
        id,
        authorId,
        author,
        authorJob,
        authorAvatar,
        content,
        OffsetDateTime.parse(datetime),
        OffsetDateTime.parse(published),
        coords,
        type,
        likeOwnerIds,
        likedByMe,
        speakerIds,
        participantsIds,
        participatedByMe,
        attachment,
        link,
        users,
        ownedByMe,
    )

    companion object {
        fun fromDto(event: Event) = EventEntity(
            event.id,
            event.authorId,
            event.author,
            event.authorJob,
            event.authorAvatar,
            event.content,
            event.datetime.toString(),
            event.published.toString(),
            event.coords,
            event.type,
            event.likeOwnerIds,
            event.likedByMe,
            event.speakerIds,
            event.participantsIds,
            event.participatedByMe,
            event.attachment,
            event.link,
            event.users,
            event.ownedByMe,
        )
    }
}

fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity.Companion::fromDto)