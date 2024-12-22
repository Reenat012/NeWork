package com.example.nework2.repositoryImpl

import com.example.nework2.api.UserApiService
import com.example.nework2.auth.AppAuth
import com.example.nework2.error.NetworkError
import com.example.nework2.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
    private var appAuth: AppAuth
) : AuthRepository {

    override suspend fun auth(login: String, password: String) {
        try {
            val response = apiService.updateUser(login, password)

            //если что-то пошло не так
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val answer = response.body() ?: throw RuntimeException("Response body is null")

            appAuth.setAuth(answer.id, answer.token)

        } catch (e: IOException) {
            throw NetworkError
        }
    }
}