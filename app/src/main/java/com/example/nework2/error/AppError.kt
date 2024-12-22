package com.example.nework2.error

sealed class AppError(var code: String): RuntimeException()
class ApiError(val status: Int, code: String): AppError(code)
object NetworkError : AppError("error_network")
object UnknownError: AppError("error_unknown")