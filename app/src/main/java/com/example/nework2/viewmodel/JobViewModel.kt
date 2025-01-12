package com.example.nework2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nework2.dto.Job
import com.example.nework2.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class JobViewModel @Inject constructor(
    private val jobRepository: JobRepository,
) : ViewModel() {
    val data: LiveData<List<Job>> = jobRepository.dataJob


    fun getJobs(userId: Long?) = viewModelScope.launch {
        if (userId == null) {
            jobRepository.getMyJobs()
        } else {
            jobRepository.getJobs(userId)
        }
    }

    fun saveJob(
        name: String,
        position: String,
        link: String?,
        startWork: OffsetDateTime,
        finishWork: OffsetDateTime
    ) = viewModelScope.launch {
        jobRepository.saveJob(
            Job(
                id = 0,
                name = name,
                position = position,
                link = link,
                start = startWork,
                finish = finishWork,
            )
        )
    }

    fun deleteJob(id: Long) = viewModelScope.launch {
        jobRepository.deleteJob(id)
    }


}