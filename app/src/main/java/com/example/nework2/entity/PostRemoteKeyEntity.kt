package com.example.nework2.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class PostRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val key: Long
) {
    enum class KeyType {
        AFTER,
        BEFORE
    }
}