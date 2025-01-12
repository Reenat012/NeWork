package com.example.nework2.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nework2.dao.EventDao
import com.example.nework2.dao.EventRemoteKeyDao
import com.example.nework2.dao.PostDao
import com.example.nework2.dao.PostRemoteKeyDao
import com.example.nework2.dao.UserDao
import com.example.nework2.entity.EventEntity
import com.example.nework2.entity.EventRemoteKeyEntity
import com.example.nework2.entity.PostEntity
import com.example.nework2.entity.PostRemoteKeyEntity
import com.example.nework2.entity.UserEntity

@Database(
    entities = [
        PostEntity::class,
        PostRemoteKeyEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
        UserEntity::class,
    ], version = 1
)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun userDao(): UserDao
}