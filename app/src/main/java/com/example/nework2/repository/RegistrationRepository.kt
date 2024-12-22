package com.example.nework2.repository

import com.example.nework2.model.ModelPhoto

interface RegistrationRepository {
    suspend fun registerUser(login: String, pass: String, name: String)
    suspend fun registerUserWithPhoto(login: String, pass: String, name: String, avatar: ModelPhoto)
}