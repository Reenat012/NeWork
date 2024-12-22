package com.example.nework2.api

import com.google.android.gms.common.GoogleApiAvailability
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class GoogleApiAvailabilityModule {
    @Provides
    @Singleton
    fun provideGoogleApiAvailability(): GoogleApiAvailability {
        return GoogleApiAvailability.getInstance()
    }
}