package com.example.nework2.dto

import com.example.nework2.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType
)
