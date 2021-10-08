package com.kshitijpatil.tazabazar.data.util

import arrow.core.Either
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.SerializationException
import com.kshitijpatil.tazabazar.data.UnknownException
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

interface LocalDateTimeSerializer {
    fun serialize(dateTime: LocalDateTime): Either<DataSourceException, String>
    fun deserialize(dateTimeString: String): Either<DataSourceException, LocalDateTime>
}

class DefaultLocalDateTimeSerializer : LocalDateTimeSerializer {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun serialize(dateTime: LocalDateTime): Either<DataSourceException, String> {
        return Either.catch {
            dateTime.format(formatter)
        }.mapLeft {
            if (it is DateTimeException) SerializationException
            else UnknownException(it)
        }
    }

    override fun deserialize(dateTimeString: String): Either<DataSourceException, LocalDateTime> {
        return Either.catch {
            formatter.parse(dateTimeString, LocalDateTime::from)
        }.mapLeft {
            if (it is DateTimeParseException) SerializationException
            else UnknownException(it)
        }
    }
}