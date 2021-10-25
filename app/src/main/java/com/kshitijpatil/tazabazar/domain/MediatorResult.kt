package com.kshitijpatil.tazabazar.domain

enum class ResponseOrigin {
    LOCAL, REMOTE
}

sealed class MediatorResult<out R> {
    abstract val responseOrigin: ResponseOrigin

    data class Success<out T>(
        val data: T,
        override val responseOrigin: ResponseOrigin
    ) : MediatorResult<T>()

    data class Error(
        val exception: Throwable,
        override val responseOrigin: ResponseOrigin
    ) : MediatorResult<Nothing>()

    data class Loading(override val responseOrigin: ResponseOrigin) : MediatorResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data, origin=$responseOrigin]"
            is Error -> "Error[exception=$exception, origin=$responseOrigin]"
            is Loading -> "Loading[origin=$responseOrigin]"
        }
    }

    fun isRemoteOrigin(): Boolean = responseOrigin == ResponseOrigin.REMOTE
}

val <T> MediatorResult<T>.data: T?
    get() = (this as? MediatorResult.Success)?.data