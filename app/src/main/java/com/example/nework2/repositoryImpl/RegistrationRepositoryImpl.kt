package com.example.nework2.repositoryImpl

import com.example.nework2.api.UserApiService
import com.example.nework2.auth.AppAuth
import com.example.nework2.error.NetworkError
import com.example.nework2.model.ModelPhoto
import com.example.nework2.repository.RegistrationRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject

class RegistrationRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
    private val appAuth: AppAuth
) : RegistrationRepository {

    override suspend fun registerUser(login: String, pass: String, name: String) {
        try {
            val response = apiService.registerUser(login, pass, name)

            //если что-то пошло не так
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val answer = response.body() ?: throw RuntimeException("Response body is null")

            appAuth.setAuth(answer.id, answer.token)

        } catch (e: IOException) {
            throw NetworkError
        }
//        catch (e: Exception) {
//            throw UnknownError
//        }
    }

    override suspend fun registerUserWithPhoto(
        login: String,
        pass: String,
        name: String,
        avatar: ModelPhoto
    ) {
        try {
            val response = apiService.registerWithPhoto(
                login.toRequestBody("text/plain".toMediaType()),
                pass.toRequestBody("text/plain".toMediaType()),
                name.toRequestBody("text/plain".toMediaType()),
                MultipartBody.Part.createFormData(
                    "file",
                    avatar.file.name,
                    avatar.file.asRequestBody()
                )
            )

            //если что-то пошло не так
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val answer = response.body() ?: throw RuntimeException("Response body is null")

            appAuth.setAuth(answer.id, answer.token)

        } catch (e: IOException) {
            throw NetworkError
        }
//        catch (e: Exception) {
//            throw UnknownError
//        }
    }
}