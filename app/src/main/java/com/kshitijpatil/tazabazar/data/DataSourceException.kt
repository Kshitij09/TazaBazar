package com.kshitijpatil.tazabazar.data

import okhttp3.ResponseBody

sealed interface DataSourceException
sealed interface RemoteSourceException : DataSourceException
sealed interface DatabaseException : DataSourceException
object PreferenceStorageException : DataSourceException
object SerializationException : DataSourceException

object ConnectivityException : DataSourceException, RemoteSourceException
data class UnknownException(val ex: Throwable) : DataSourceException
object EmptyBodyException : DataSourceException, RemoteSourceException
data class ApiException(val statusCode: Int, val errorBody: ResponseBody?) : DataSourceException,
    RemoteSourceException