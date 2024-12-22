plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.nework2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nework2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx.v1150)
    implementation(libs.androidx.appcompat.v170)
    implementation(libs.material.v1120)
    implementation(libs.androidx.constraintlayout.v220)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.navigation.fragment.ktx.v285)
    implementation(libs.androidx.core.ktx.v1150)
    implementation(libs.gson)
    implementation(libs.androidx.navigation.fragment.ktx.v285)
    implementation(libs.androidx.navigation.ui.ktx.v285)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    implementation(libs.firebase.messaging.ktx)
    kapt(libs.hilt.compiler)
    implementation(libs.okhttp)
    implementation(libs.generativeai)
    implementation(libs.glide)
    ksp(libs.ksp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.room.ktx)
    implementation(libs.annotations)
    implementation(libs.androidx.swiperefreshlayout.v120alpha01)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.play.services.cast.framework)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.espresso.core.v361)
    implementation(libs.imagepicker)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.google.firebase.messaging.ktx)
    implementation(libs.play.services.base)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)

    implementation(libs.hilt.android)


    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.room.paging)
}