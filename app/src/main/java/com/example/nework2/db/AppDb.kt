package com.example.nework2.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nework2.dao.PostDao
import com.example.nework2.dao.PostRemoteKeyDao
import com.example.nework2.entity.PostEntity
import com.example.nework2.entity.PostRemoteKeyEntity

//библиотека Room
@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class], version = 2, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}