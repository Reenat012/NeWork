package com.example.nework2.service

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.nework2.R
import java.lang.String.format
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class WallService {
    //функция для правильного отображения лайков, репостов, просмотров
    fun amount(count: Int): String {
        val df =
            DecimalFormat("#.#") //функция для ограничения двойного числа до 2-х десятичных точек с использованием шаблона #.##
        df.roundingMode = RoundingMode.DOWN

        return when (count) {
            in 0..999 -> "$count"
            in 1_000..9_999 -> "${df.format(count / 1_000.0)}K"
            in 10_000..999_999 -> "${(count / 10_000)}K"
            in 1_000_000..999_999_999 -> "${df.format(count / 1_000_000.0)}M"
            else -> "Бесконечность"
        }
    }

    //расчет сколько времени назад был опубликован пост
    @RequiresApi(Build.VERSION_CODES.O)
    fun datePubl(publishDate: String) : String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val localPublishDate = LocalDateTime.parse(publishDate, formatter)
        //получаем текущую дату
        val currentDate = LocalDateTime.now()
        //вычисляем количество дней между текущей датой и датой публикации
        val timeBetween = ChronoUnit.MINUTES.between(localPublishDate, currentDate)

        return when (timeBetween) {
            in 0..1 -> "только что"
            in 2..59 -> "$timeBetween минут назад"
            in 60..60 * 24 -> "${timeBetween.toInt()} часов назад"
            else -> "$localPublishDate"
        }
    }
}
