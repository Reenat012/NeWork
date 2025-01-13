package com.example.nework2.repositoryImpl

import android.content.Context
import android.widget.Toast
import androidx.paging.ExperimentalPagingApi
import com.example.nework2.R
import com.example.nework2.api.UserApiService
import com.example.nework2.auth.AppAuth
import com.example.nework2.error.ApiError
import com.example.nework2.error.NetworkError
import com.example.nework2.error.UnknownError
import com.example.nework2.model.AttachmentModel
import com.example.nework2.model.AuthModel
import com.example.nework2.repository.AuthRepository
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val apiService: UserApiService,
    private var appAuth: AppAuth
) : AuthRepository {

    override val dataAuth: StateFlow<AuthModel> = appAuth.authState

    override suspend fun register(
        login: String,
        name: String,
        pass: String,
        attachmentModel: AttachmentModel?
    ) {
        try {
            val response = if (attachmentModel != null) {
                val part = MultipartBody.Part.createFormData(
                    "file",
                    attachmentModel.file.name,
                    attachmentModel.file.asRequestBody()
                )
                apiService.usersRegistrationWithPhoto(login, pass, name, part)
            } else {
                apiService.usersRegistration(login, pass, name)
            }

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> {
                        toastMsg(context.getString(R.string.the_user_is_already_registered))
                    }

                    415 -> {
                        toastMsg(context.getString(R.string.incorrect_photo_format))
                    }

                    else -> throw ApiError(response.code(), response.message())
                }
                return
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appAuth.setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun login(login: String, pass: String) {
        try {
            val response = apiService.usersAuthentication(login, pass)
            if (!response.isSuccessful) {
                when (response.code()) {
                    400 -> {
                        toastMsg(context.getString(R.string.incorrect_password))
                    }

                    404 -> {
                        toastMsg(context.getString(R.string.user_unregistered))
                    }

                    else -> throw ApiError(response.code(), response.message())
                }
                return
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appAuth.setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun logout() {
        appAuth.removeAuth()
    }

    private fun toastMsg(msg: String) {
        Toast.makeText(
            context,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }
}