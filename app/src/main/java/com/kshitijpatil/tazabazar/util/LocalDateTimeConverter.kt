package com.kshitijpatil.tazabazar.util

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object LocalDateTimeConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun toLocalDateTime(value: String): LocalDateTime {
        return formatter.parse(value, LocalDateTime::from)
    }

    fun fromLocalDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(formatter)
    }
}