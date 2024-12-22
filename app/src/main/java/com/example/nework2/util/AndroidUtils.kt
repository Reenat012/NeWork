package com.example.nework2.util

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun View.focusAndShowKeyboard() {
    /**
     * Это должно быть вызвано, когда окно уже имеет фокус
     */
    fun View.showTheKeyboardNow() {
        if (isFocused) {
            post {
                // Мы все равно отправляем вызов, на всякий случай, если нас уведомят о фокусе Windows
                // но InputMethodManager еще не был настроен должным образом.
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        // Не нужно ждать, пока окно сфокусируется
        showTheKeyboardNow()
    } else {
        // Нам нужно подождать, пока окно не сфокусируется.
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    // Это уведомление придет непосредственно перед настройкой InputMethodManager.
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        // Очень важно удалить этот прослушиватель, как только мы закончим.
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}