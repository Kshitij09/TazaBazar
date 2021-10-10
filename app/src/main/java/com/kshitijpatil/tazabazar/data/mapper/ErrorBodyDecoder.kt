package com.kshitijpatil.tazabazar.data.mapper

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.kshitijpatil.tazabazar.api.dto.ApiError
import com.kshitijpatil.tazabazar.data.SerializationException
import com.squareup.moshi.JsonAdapter
import okhttp3.ResponseBody
import timber.log.Timber


class ErrorBodyDecoder(private val jsonAdapter: JsonAdapter<ApiError>) :
    Mapper<ResponseBody, Either<SerializationException, ApiError>> {
    override fun map(from: ResponseBody): Either<SerializationException, ApiError> {
        return Either.catch {
            return jsonAdapter.fromJson(from.source())
                .rightIfNotNull { SerializationException }

        }.mapLeft {
            Timber.e(it, "Failed decoding errorBody: $from")
            SerializationException
        }
    }
}