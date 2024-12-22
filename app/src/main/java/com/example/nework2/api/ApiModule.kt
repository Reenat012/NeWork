package com.example.nework2.api

import com.example.nework2.auth.AppAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    companion object {
        private const val BASE_URL = "http://94.228.125.136:8080/api/slow/"
    }

    @Provides
    @Singleton
    fun provideFirebaseMessagingModule(): FirebaseMessagingModule {
        return FirebaseMessagingModule()
    }

    @Provides
    @Singleton
    fun provideGoogleApiAvailabilityModule(): GoogleApiAvailabilityModule {
        return GoogleApiAvailabilityModule()
    }

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if ("true".toBoolean()) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS) //30 сек будем ждать вызова клиента
        .addInterceptor(logging)
        .addInterceptor { chain ->
            chain.proceed(
                //либо у нас есть токен и создается билдер с новым заголовком
                appAuth.data.value?.token?.let {
                    chain.request().newBuilder()
                        .addHeader("Authorization", it)
                        .build()
                }
                //либо заголовок остается неизменным
                    ?: chain.request()
            )
        }
        .build()

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient) : Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .build()

    @Provides
    @Singleton
    fun providePostApiService(retrofit: Retrofit): PostApiService = retrofit.create<PostApiService>()

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService = retrofit.create<UserApiService>()

    @Provides
    @Singleton
    fun providePushApiService(retrofit: Retrofit): PushApiService = retrofit.create<PushApiService>()

}