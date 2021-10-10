package com.kshitijpatil.tazabazar.test.util

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.SerializationException
import com.kshitijpatil.tazabazar.data.mapper.EitherStringSerializer
import com.kshitijpatil.tazabazar.model.LoggedInUser

class FakeLoggedInUserSerializer(private val deserialized: LoggedInUser? = null) :
    EitherStringSerializer<LoggedInUser> {
    override fun serialize(item: LoggedInUser): Either<DataSourceException, String> {
        return item.toString().right()
    }

    override fun deserialize(raw: String): Either<DataSourceException, LoggedInUser> {
        return deserialized?.right() ?: SerializationException.left()
    }
}