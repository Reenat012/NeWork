package com.example.nework2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.nework2.auth.AppAuth
import com.example.nework2.dto.Token
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val appAuth: AppAuth) : ViewModel() {
    //данные авторизации
    val authData: LiveData<Token?> = appAuth
        .data.asLiveData()

    //проверяем авторизован пользователь или нет
    val isAuthenticated: Boolean
        get() = authData.value?.token != null
}