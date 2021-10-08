package com.kshitijpatil.tazabazar.data.util

import arrow.core.Either
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.SerializationException
import com.kshitijpatil.tazabazar.data.UnknownException
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException

abstract class LoggedInUserSerializer {
    operator fun invoke(user: LoggedInUser) = serialize(user)
    abstract fun serialize(user: LoggedInUser): Either<DataSourceException, String>
}

class LoggedInUserSerializerImpl(private val jsonAdapter: JsonAdapter<LoggedInUser>) :
    LoggedInUserSerializer() {
    override fun serialize(user: LoggedInUser): Either<DataSourceException, String> {
        return Either.catch {
            jsonAdapter.toJson(user)
        }.mapLeft {
            if (it is JsonDataException) SerializationException
            else UnknownException(it)
        }
    }
}