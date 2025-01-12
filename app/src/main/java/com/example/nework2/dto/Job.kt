package com.example.nework2.dto

import java.time.OffsetDateTime

data class Job(
    val id: Long,
    val name: String,
    val position: String,
    val start: OffsetDateTime,
    val finish: OffsetDateTime,
    val link: String? = null
)
