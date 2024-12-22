package com.example.nework2.repository

interface AuthRepository {
    suspend fun auth(login: String, password: String)
}