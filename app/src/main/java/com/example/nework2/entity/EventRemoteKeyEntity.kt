package com.example.nework2.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventRemoteKeyEntity(
    @PrimaryKey
    val type: PostRemoteKeyEntity.KeyType,
    val id: Long
)
