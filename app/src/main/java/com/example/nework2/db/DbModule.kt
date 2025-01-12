package com.example.nework2.db

import android.content.Context
import androidx.room.Room
import com.example.nework2.dao.PostDao
import com.example.nework2.dao.PostRemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.nework2.dao.EventDao
import com.example.nework2.dao.EventRemoteKeyDao
import com.example.nework2.dao.UserDao


@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext
        context: Context
    ): AppDb = Room.databaseBuilder(context, AppDb::class.java, "app.db").build()

    @Provides
    fun providePostDao(
        appDb: AppDb
    ): PostDao = appDb.postDao()

    @Provides
    fun providePostRemoteKeyDao(
        appDb: AppDb
    ): PostRemoteKeyDao = appDb.postRemoteKeyDao()

    @Provides
    fun provideEventDao(
        appDb: AppDb
    ): EventDao = appDb.eventDao()

    @Provides
    fun provideEventRemoteKeyDao(
        appDb: AppDb
    ): EventRemoteKeyDao = appDb.eventRemoteKeyDao()

    @Provides
    fun provideUserDao(
        appDb: AppDb
    ): UserDao = appDb.userDao()
}