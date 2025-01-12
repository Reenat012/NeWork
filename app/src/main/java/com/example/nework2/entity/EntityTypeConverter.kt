package com.example.nework2.entity

import androidx.room.TypeConverter
import com.example.nework2.dto.Attachment
import com.example.nework2.dto.Coordinates
import com.example.nework2.dto.UserPreview
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CoordsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromCoords(coords: Coordinates?): String? {
        return if (coords != null) gson.toJson(coords) else null
    }

    @TypeConverter
    fun toCoords(coords: String?): Coordinates? {
        return if (coords != null) gson.fromJson(coords, Coordinates::class.java) else null
    }
}

class MentionIdsConverter {
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Long>>() {}.type

    @TypeConverter
    fun fromMentionIds(mentionIds: List<Long>?): String? {
        return if (mentionIds != null) gson.toJson(mentionIds) else null
    }

    @TypeConverter
    fun toMentionIds(mentionIds: String?): List<Long>? {
        return if (mentionIds != null) gson.fromJson(mentionIds, typeToken) else null
    }

}

class AttachmentConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromAttachment(attachment: Attachment?): String? {
        return if (attachment != null) gson.toJson(attachment) else null
    }

    @TypeConverter
    fun toAttachment(attachment: String?): Attachment? {
        return if (attachment != null) gson.fromJson(
            attachment,
            Attachment::class.java
        ) else null
    }
}

class UsersConverter {
    private val gson = Gson()
    private val typeToken = object : TypeToken<Map<String, UserPreview>>() {}.type

    @TypeConverter
    fun fromAttachment(users: Map<String, UserPreview>?): String? {
        return if (users != null) gson.toJson(users) else null
    }

    @TypeConverter
    fun toAttachment(users: String?): Map<String, UserPreview>? {
        return if (users != null) gson.fromJson(users, typeToken) else null
    }
}