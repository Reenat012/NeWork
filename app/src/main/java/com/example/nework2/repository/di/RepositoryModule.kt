package com.example.nework2.repository.di

import com.example.nework2.repository.AuthRepository
import com.example.nework2.repository.PostRepository
import com.example.nework2.repositoryImpl.AuthRepositoryImpl
import com.example.nework2.repositoryImpl.PostRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import javax.sql.DataSource

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Binds
    fun bindsPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    fun bindsAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}