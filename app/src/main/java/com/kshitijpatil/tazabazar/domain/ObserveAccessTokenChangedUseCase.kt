package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.local.prefs.PreferenceStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Event token indicating access token stored in the
 * [PreferenceStorage] was recently changed
 */
object AccessTokenChanged

class ObserveAccessTokenChangedUseCase(
    dispatcher: CoroutineDispatcher,
    private val externalScope: CoroutineScope,
    private val preferenceStorage: PreferenceStorage
) : FlowProducerUseCase<AccessTokenChanged>(dispatcher) {
    private val upstreamChannel = Channel<AccessTokenChanged>(capacity = Channel.CONFLATED)

    init {
        externalScope.launch { collectAccessTokenPreferenceChanges() }
    }

    /**
     * We want to keep track of token changes regardless of any subscriber
     * attached to it. Any non-null changes are thus conflated in the [upstreamChannel]
     */
    private suspend fun collectAccessTokenPreferenceChanges() {
        preferenceStorage.accessToken
            .filterNotNull() // don't emit if session was cleared
            .map { AccessTokenChanged }
            .collect { upstreamChannel.send(it) }
    }

    override fun createObservable(): Flow<AccessTokenChanged> {
        return upstreamChannel.receiveAsFlow()
            .distinctUntilChanged()
            .shareIn(externalScope, SharingStarted.Eagerly, replay = 1)
    }

}