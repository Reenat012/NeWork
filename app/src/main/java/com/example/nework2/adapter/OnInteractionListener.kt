package com.example.nework2.adapter

import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.UserResponse

interface OnInteractionListener {
    fun like(feedItem: FeedItem)
    fun delete(feedItem: FeedItem)
    fun edit(feedItem: FeedItem)
    fun selectUser(userResponse: UserResponse)
    fun openCard(feedItem: FeedItem)
}