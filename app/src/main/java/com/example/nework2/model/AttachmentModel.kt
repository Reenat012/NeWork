package com.example.nework2.model

import android.net.Uri
import com.example.nework2.enumeration.AttachmentType
import java.io.File

data class AttachmentModel(
    val attachmentType: AttachmentType,
    val uri: Uri,
    val file: File,
)