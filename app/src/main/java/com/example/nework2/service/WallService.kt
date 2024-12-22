package com.example.nework2.service

import java.math.RoundingMode
import java.text.DecimalFormat

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
}
