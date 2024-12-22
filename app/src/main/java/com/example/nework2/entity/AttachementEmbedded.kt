package com.example.nework2.entity

import androidx.room.PrimaryKey
import com.example.nework2.dto.Attachment
import com.example.nework2.enumeration.AttachmentType

data class AttachementEmbedded(
    @PrimaryKey(autoGenerate = true)
    val url: String,
    val type: AttachmentType,
) {

    fun toDto() = Attachment(url, type)
    companion object {
        fun fromDto(dto: Attachment) : AttachementEmbedded = with(dto) {
            AttachementEmbedded(
                url, type
            )
        }
    }
}