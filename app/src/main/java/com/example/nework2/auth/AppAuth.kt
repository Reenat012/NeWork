package com.example.nework2.auth

import android.content.Context
import androidx.core.content.edit
import com.example.nework2.api.PushApiService
import com.example.nework2.dto.PushToken
import com.example.nework2.dto.Token
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
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

    //хранилище данных
    private val _data = MutableStateFlow<Token?>(null)
    private val ID_KEY = "id"
    private val TOKEN_KEY = "token"

    //публичная ссылка на данные
    val data: StateFlow<Token?> = _data.asStateFlow()

    init {
        //считываем id и token
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id != 0L && token != null) {
            //записываем id и token в хранилище данных _data
            _data.value = Token(id, token)
        } else {
            //чистим preference
            prefs.edit { clear() }
        }

        sendPushToken()
    }

    //сохранить аутентификацию
    @Synchronized //чтобы только один поток мог заходить и обновлять
    fun setAuth(id: Long, token: String) {
        prefs.edit {
            //функция, используемая для сохранения длинного целого числа (пара ключ-значение)
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)


            //обновляем хранилище _data
            _data.value = Token(id, token)
        }

        sendPushToken()
    }

    //очистить аутентификацию
    @Synchronized
    fun clearAuth() {
        prefs.edit { clear() }
        _data.value = null

        sendPushToken()
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