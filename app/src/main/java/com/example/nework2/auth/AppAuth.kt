package com.example.nework2.auth

import android.content.Context
import androidx.core.content.edit
import com.example.nework2.api.PushApiService
import com.example.nework2.dto.PushToken
import com.example.nework2.model.AuthModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)


    private val idKey = "id"
    private val tokenKey = "token"
    //хранилище данных
    private val _authState = MutableStateFlow(
        AuthModel(
            prefs.getLong(idKey, 0L),
            prefs.getString(tokenKey, null)
        )
    )

    //публичная ссылка на данные
    val authState: StateFlow<AuthModel> = _authState.asStateFlow()

    init {
        //считываем id и token
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)

        if (id != 0L && token != null) {
            //записываем id и token в хранилище данных _data
            _authState.value = AuthModel(id, token)
        } else {
            //чистим preference
            prefs.edit { clear() }
        }

        sendPushToken()
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthModel(id, token)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            commit()
        }
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthModel()
        with(prefs.edit()) {
            clear()
            commit()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppAuthEntryPoint {
        fun getPushApiService(): PushApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val dto = PushToken(token ?: FirebaseMessaging.getInstance().token.await())

                val entryPoint =
                    EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getPushApiService().sendPushToken(dto)
            } catch (e: Exception) {
                e.printStackTrace()
                //ignore
            }
        }
    }
}