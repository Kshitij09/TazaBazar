package com.kshitijpatil.tazabazar.data

import arrow.core.Option
import com.squareup.moshi.JsonDataException
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.IOException

sealed interface DataSourceException
sealed interface RemoteSourceException : DataSourceException
sealed interface DatabaseException : DataSourceException
object PreferenceStorageException : DataSourceException
object SerializationException : DataSourceException

object ConnectivityException : RemoteSourceException
data class InternalServerException(val ex: Throwable) : RemoteSourceException
data class UnknownException(val ex: Throwable) : DataSourceException
object EmptyBodyException : RemoteSourceException
data class ApiException(val statusCode: Int, val errorBody: ResponseBody?) : RemoteSourceException

/**
 * Map Common retrofit, moshi, okhttp exceptions
 * optionally provide a [tag] to enable logging
 * @return DataSourceException or null
 */
fun Throwable.mapCommonNetworkExceptions(tag: String?): Option<DataSourceException> {
    val ex = when (this) {
        is IOException -> {
            tag?.let { Timber.d("$tag: Connectivity Error") }
            ConnectivityException
        }
        is JsonDataException -> {
            tag?.let { Timber.d("$it: Json Parsing error") }
            SerializationException
        }
        else -> null
    }
    return Option.fromNullable(ex)
}