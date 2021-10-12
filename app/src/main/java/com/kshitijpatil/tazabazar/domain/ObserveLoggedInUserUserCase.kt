package com.kshitijpatil.tazabazar.domain

import arrow.core.Either
import com.kshitijpatil.tazabazar.data.local.prefs.PreferenceStorage
import com.kshitijpatil.tazabazar.data.mapper.EitherStringSerializer
import com.kshitijpatil.tazabazar.model.LoggedInUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*

class ObserveLoggedInUserUseCase(
    dispatcher: CoroutineDispatcher,
    private val preferenceStorage: PreferenceStorage,
    private val loggedInUserSerializer: EitherStringSerializer<LoggedInUser>,
) : FlowProducerUseCase<LoggedInUser?>(dispatcher) {

    override fun createObservable(): Flow<LoggedInUser?> {
        return preferenceStorage.userDetails
            .map { userJson ->
                userJson ?: return@map null
                when (val loggedInUser = loggedInUserSerializer.deserialize(userJson)) {
                    is Either.Left -> null
                    is Either.Right -> loggedInUser.value
                }
            }
    }
}