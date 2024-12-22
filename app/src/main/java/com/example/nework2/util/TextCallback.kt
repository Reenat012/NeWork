package com.example.nework2.util

//передача текста из фрагмента в репозиторий для обработки
interface TextCallback {
    fun onLoginReceived(text: String)
    fun onPasswordReceived(text: String)
    fun onRetryPasswordReceived(text: String)
    fun onNameReceived(text: String)

}