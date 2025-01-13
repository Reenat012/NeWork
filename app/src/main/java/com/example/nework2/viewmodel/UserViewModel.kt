package com.example.nework2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.example.nework2.dto.FeedItem
import com.example.nework2.dto.UserResponse
import com.example.nework2.repository.Repository
import com.example.nework2.repository.UserRepository
import com.example.nework2.repositoryImpl.RepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: RepositoryImpl
) : ViewModel() {
    val dataUsers: Flow<PagingData<FeedItem>> =
        userRepository.dataUsers.map {
            it.map { feedItem ->
                feedItem
            }
        }.flowOn(Dispatchers.Default)

    private val _dataUser = MutableLiveData<UserResponse>(null)
    val dataUser: LiveData<UserResponse> = _dataUser

    fun getUser(userId: Long) = viewModelScope.launch {
        _dataUser.value = userRepository.getUser(userId)
    }

}