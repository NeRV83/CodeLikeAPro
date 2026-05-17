package ru.netology.nmedia.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Utility {
    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'г.'", Locale("ru"))
        return currentDate.format(formatter).toString()
    }

    fun formatShortNumber(number: Int): String {
        return when {
            number < 1000 -> number.toString()
            number < 10_000 -> {
                val thousands = number / 1000.0
                val rounded = (thousands * 10).toInt() / 10.0
                "${rounded}K".replace(".0K", "K")
            }

            number < 1_000_000 -> {
                val thousands = number / 1000
                "${thousands}K"
            }

            number < 10_000_000 -> {
                val millions = number / 1_000_000.0
                val rounded = (millions * 10).toInt() / 10.0
                "${rounded}M".replace(".0M", "M")
            }

            else -> {
                val millions = number / 1_000_000
                "${millions}M"
            }
        }
    }
}