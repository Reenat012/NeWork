package com.example.nework2.repositoryImpl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.nework2.api.JobApiService
import com.example.nework2.dto.Job
import com.example.nework2.error.ApiError
import com.example.nework2.error.NetworkError
import com.example.nework2.error.UnknownError
import com.example.nework2.repository.JobRepository
import java.io.IOException

class JobRepositoryImpl(
    private val apiService: JobApiService,
) : JobRepository {

    private val _dataJob = MutableLiveData<List<Job>>()
    override val dataJob: LiveData<List<Job>> = _dataJob

    override suspend fun getMyJobs() {
        try {
            val response = apiService.myJobGetAllJob()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            _dataJob.value = body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getJobs(userId: Long) {
        try {
            val response = apiService.jobsGetAllJob(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            _dataJob.value = body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveJob(job: Job) {
        try {
            val response = apiService.myJobSaveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())

            getMyJobs()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun deleteJob(id: Long) {
        try {
            val response = apiService.myJobDeleteJob(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            _dataJob.value = _dataJob.value?.filter { it.id != id }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}