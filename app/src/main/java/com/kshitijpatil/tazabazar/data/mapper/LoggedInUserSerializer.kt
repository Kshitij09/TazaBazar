package com.kshitijpatil.tazabazar.data.mapper

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.SerializationException
import com.kshitijpatil.tazabazar.data.UnknownException
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import timber.log.Timber

class LoggedInUserSerializer(
    private val jsonAdapter: JsonAdapter<LoggedInUser>
) : EitherStringSerializer<LoggedInUser> {

    override fun serialize(item: LoggedInUser): Either<DataSourceException, String> {
        return Either.catch {
            jsonAdapter.toJson(item)
        }.mapLeft {
            if (it is JsonDataException) {
                Timber.d("Failed serializing user: $item")
                SerializationException
            } else {
                Timber.e(it)
                UnknownException(it)
            }
        }
    }

    override fun deserialize(raw: String): Either<DataSourceException, LoggedInUser> {
        return Either.catch {
            return jsonAdapter.fromJson(raw).rightIfNotNull {
                SerializationException
            }
        }.mapLeft {
            if (it is JsonDataException) {
                Timber.d("Failed parsing LoggedInUser from $raw")
                SerializationException
            } else {
                Timber.e(it)
                UnknownException(it)
            }
        }
    }
}