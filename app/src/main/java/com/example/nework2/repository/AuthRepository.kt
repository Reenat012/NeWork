package com.example.nework2.repository

import com.example.nework2.model.AttachmentModel
import com.example.nework2.model.AuthModel
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val dataAuth: StateFlow<AuthModel>

    suspend fun register(
        login: String,
        name: String,
        pass: String,
        attachmentModel: AttachmentModel?
    )

    suspend fun login(login: String, pass: String)
    fun logout()
}