package com.example.nework2.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nework2.enumeration.AttachmentType
import com.example.nework2.model.AttachmentModel
import com.example.nework2.model.AuthModel
import com.example.nework2.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val dataAuth: LiveData<AuthModel> = authRepository.dataAuth.asLiveData(Dispatchers.Default)

    private val _photoData: MutableLiveData<AttachmentModel?> = MutableLiveData(null)
    val photoData: LiveData<AttachmentModel?>
        get() = _photoData

    fun register(login: String, name: String, pass: String) {
        viewModelScope.launch {
            val photo = _photoData.value
            authRepository.register(login, name, pass, photo)
        }
    }

    fun login(login: String, pass: String) {
        viewModelScope.launch {
            authRepository.login(login, pass)
        }
    }

    fun setPhoto(uri: Uri, file: File) {
        _photoData.value = AttachmentModel(AttachmentType.IMAGE, uri, file)
    }

    fun logout() {
        authRepository.logout()
    }
}