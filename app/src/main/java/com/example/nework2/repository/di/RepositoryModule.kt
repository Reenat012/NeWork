package com.example.nework2.repository.di

import com.example.nework2.repository.AuthRepository
import com.example.nework2.repository.PostRepository
import com.example.nework2.repository.RegistrationRepository
import com.example.nework2.repositoryImpl.AuthRepositoryImpl
import com.example.nework2.repositoryImpl.PostRepositoryImpl
import com.example.nework2.repositoryImpl.RegistrationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Binds
    fun bindsPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    fun bindsAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    fun bindsRegistrRepository(impl: RegistrationRepositoryImpl): RegistrationRepository
}