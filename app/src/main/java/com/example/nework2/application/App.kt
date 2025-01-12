package com.example.nework2.application

import dagger.hilt.android.HiltAndroidApp
import android.app.Application
import com.example.nework2.BuildConfig
import com.yandex.mapkit.MapKitFactory

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}