package com.example.nework2.model

import com.example.nework2.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false
)

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false
)