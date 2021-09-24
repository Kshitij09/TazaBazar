package com.kshitijpatil.tazabazar.data.local

import androidx.room.TypeConverter
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

object TazaBazarTypeConverters {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    @JvmStatic
    fun toOffsetDateTime(value: String): OffsetDateTime {
        return formatter.parse(value, OffsetDateTime::from)
    }

    @TypeConverter
    @JvmStatic
    fun fromOffsetDateTime(date: OffsetDateTime): String? {
        return date.format(formatter)
    }
}