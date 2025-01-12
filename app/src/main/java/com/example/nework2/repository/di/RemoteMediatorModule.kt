package com.example.nework2.repository.di

import com.example.nework2.api.EventApiService
import com.example.nework2.api.PostApiService
import com.example.nework2.api.UserApiService
import com.example.nework2.dao.EventDao
import com.example.nework2.dao.EventRemoteKeyDao
import com.example.nework2.dao.PostDao
import com.example.nework2.dao.PostRemoteKeyDao
import com.example.nework2.dao.UserDao
import com.example.nework2.db.AppDb
import com.example.nework2.repository.EventRemoteMediator
import com.example.nework2.repository.PostRemoteMediator
import com.example.nework2.repository.UserRemoteMediator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class RemoteMediatorModule {

    @Singleton
    @Provides
    fun providePostRemoteMediator(
        postApiService: PostApiService,
        appDb: AppDb,
        postDao: PostDao,
        postRemoteKeyDao: PostRemoteKeyDao
    ): PostRemoteMediator = PostRemoteMediator(postApiService, appDb, postDao, postRemoteKeyDao)

    @Singleton
    @Provides
    fun provideEventRemoteMediator(
        eventApiService: EventApiService,
        appDb: AppDb,
        eventDao: EventDao,
        eventRemoteKeyDao: EventRemoteKeyDao
    ): EventRemoteMediator = EventRemoteMediator(eventApiService, appDb, eventDao, eventRemoteKeyDao)

    @Singleton
    @Provides
    fun provideUserRemoteMediator(
        userApiService: UserApiService,
        userDao: UserDao
    ): UserRemoteMediator = UserRemoteMediator(userApiService, userDao)

}