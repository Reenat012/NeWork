package com.example.nework2.api

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.nework2.BuildConfig
import com.example.nework2.auth.AppAuth
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    companion object {
        private const val BASE_URL = "http://94.228.125.136:8080/"
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
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            appAuth.authState.value.token?.let { token->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .addHeader("Api-Key", BuildConfig.API_KEY)
                    .build()
                return@addInterceptor chain.proceed(request)
            }
            val request = chain.request().newBuilder()
                .addHeader("Api-Key", BuildConfig.API_KEY)
                .build()
            chain.proceed(request)
        }
        .build()

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient) : Retrofit = Retrofit.Builder()
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().registerTypeAdapter(
                    OffsetDateTime::class.java,
                    object : TypeAdapter<OffsetDateTime>() {
                        override fun write(out: JsonWriter?, value: OffsetDateTime?) {
                            out?.value(value?.toEpochSecond())
                        }

                        override fun read(jsonReader: JsonReader): OffsetDateTime {
                            return OffsetDateTime.ofInstant(
                                Instant.parse(jsonReader.nextString()),
                                ZoneId.systemDefault()
                            )
                        }
                    }).create()
            )
        )
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