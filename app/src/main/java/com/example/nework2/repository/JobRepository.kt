package com.example.nework2.repository

import androidx.lifecycle.LiveData
import com.example.nework2.dto.Job

interface JobRepository {
    val dataJob: LiveData<List<Job>>

    suspend fun getMyJobs()
    suspend fun getJobs(userId: Long)
    suspend fun saveJob(job: com.example.nework2.dto.Job)
    suspend fun deleteJob(id: Long)
}