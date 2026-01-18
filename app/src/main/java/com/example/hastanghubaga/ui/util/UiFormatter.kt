package com.example.hastanghubaga.ui.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime

object UiFormatter {
    fun formatTimeRange(
        start: LocalDateTime,
        end: LocalDateTime?
    ): String {
        fun LocalDateTime.hhmm(): String =
            "%02d:%02d".format(time.hour, time.minute)

        return if (end != null) {
            "${start.hhmm()} to ${end.hhmm()}"
        } else {
            start.hhmm()
        }
    }
}

fun LocalDateTime.format12Hour(): String {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
    return this.toJavaLocalDateTime().format(formatter)
}