package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.local.prefs.PreferenceStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * Event token indicating access token stored in the
 * [PreferenceStorage] was recently changed
 */
object AccessTokenChanged

class ObserveAccessTokenChangedUseCase(
    dispatcher: CoroutineDispatcher,
    private val preferenceStorage: PreferenceStorage
) : FlowProducerUseCase<AccessTokenChanged>(dispatcher) {
    override fun createObservable(): Flow<AccessTokenChanged> {
        return preferenceStorage.accessToken
            .filterNotNull() // don't emit if session was cleared
            .map { AccessTokenChanged }
            .conflate()
    }

}