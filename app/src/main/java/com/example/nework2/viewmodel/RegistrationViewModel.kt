package com.example.nework2.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nework2.model.ModelPhoto
import com.example.nework2.repositoryImpl.RegistrationRepositoryImpl
import com.example.nework2.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(private val registerRepository: RegistrationRepositoryImpl) : ViewModel() {
    lateinit var name: String
    lateinit var login: String
    lateinit var password: String
    lateinit var retryPassword: String

    //сохраняем фото в переменную
    private val _photo = MutableLiveData<ModelPhoto?>(null)
    val photo: LiveData<ModelPhoto?>
        get() = _photo

    private val _progressRegister = SingleLiveEvent<Unit>()
    val progressRegister: SingleLiveEvent<Unit> = _progressRegister

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: SingleLiveEvent<String> = _errorEvent

    fun setPhoto(uri: Uri, file: File) {
        _photo.value = ModelPhoto(uri, file)
    }

    fun clearPhoto() {
        _photo.value = null
    }

    fun saveName(textName: String) {
        name = textName
    }

    fun saveLogin(textLogin: String) {
        login = textLogin
    }

    fun savePassword(textPassword: String) {
        password = textPassword
    }

    fun saveRetryPassword(textRetryPassword: String) {
        retryPassword = textRetryPassword
    }

    //проверка на заполненность всех полей регистрации
    fun checkFieldRegister(): Boolean {
        return ((name != null && name != "")
                && (login != null && login != "")
                && (password != null && password != "")
                && (retryPassword != null && retryPassword != ""))
    }

    //проверка на совпадение password и retryPassword
    fun checkPassword(): Boolean {
        return password == retryPassword
    }

    fun onSignUpTap() {
        viewModelScope.launch {
            _progressRegister.postValue(Unit)
            try {
                if (photo.value != null) {
                    photo.value?.let {
                        registerRepository.registerUserWithPhoto(login, password, name, it)
                    }
                } else {
                    registerRepository.registerUser(login, password, name)
                }
            } catch (e: Exception) {
                _errorEvent.postValue("Ошибка")
            }
        }
    }
}