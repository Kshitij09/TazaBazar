package com.kshitijpatil.tazabazar.data.network

import com.kshitijpatil.tazabazar.data.ConnectivityException
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.SerializationException
import com.squareup.moshi.JsonDataException
import timber.log.Timber
import java.io.IOException

fun mapCommonNetworkExceptions(ex: Throwable): DataSourceException? {
    return when (ex) {
        is IOException -> {
            Timber.d("Connectivity Error")
            ConnectivityException
        }
        is JsonDataException -> {
            Timber.d("Json Parsing error")
            SerializationException
        }
        else -> null
    }
}