package com.kshitijpatil.tazabazar.data.mapper

import arrow.core.Either
import com.kshitijpatil.tazabazar.data.DataSourceException

interface EitherStringSerializer<D> {
    fun serialize(item: D): Either<DataSourceException, String>
    fun deserialize(raw: String): Either<DataSourceException, D>
}