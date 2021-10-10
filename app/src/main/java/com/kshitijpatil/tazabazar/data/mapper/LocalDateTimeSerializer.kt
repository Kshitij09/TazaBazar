package com.kshitijpatil.tazabazar.data.mapper

import arrow.core.Either
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.SerializationException
import com.kshitijpatil.tazabazar.data.UnknownException
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import timber.log.Timber

class LocalDateTimeSerializer : EitherStringSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun serialize(item: LocalDateTime): Either<DataSourceException, String> {
        return Either.catch {
            item.format(formatter)
        }.mapLeft {
            if (it is DateTimeException) {
                Timber.d("Failed serializing date: $item")
                SerializationException
            } else {
                Timber.e(it)
                UnknownException(it)
            }
        }
    }

    override fun deserialize(raw: String): Either<DataSourceException, LocalDateTime> {
        return Either.catch {
            formatter.parse(raw, LocalDateTime::from)
        }.mapLeft {
            if (it is DateTimeParseException) {
                Timber.d("Failed parsing date: $raw")
                SerializationException
            } else {
                Timber.e(it)
                UnknownException(it)
            }
        }
    }
}