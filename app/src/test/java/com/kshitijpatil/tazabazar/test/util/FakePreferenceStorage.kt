package com.kshitijpatil.tazabazar.test.util

import com.kshitijpatil.tazabazar.data.local.prefs.PreferenceStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferenceStorage(
    refreshToken: String? = null,
    accessToken: String? = null,
    userDetailsJson: String? = null,
    loggedInAt: String? = null,
    lastLoggedInUsername: String? = null
) : PreferenceStorage {
    private val _refreshToken = MutableStateFlow(refreshToken)
    override val refreshToken: Flow<String?> = _refreshToken

    override suspend fun setRefreshToken(refreshToken: String?) {
        _refreshToken.value = refreshToken
    }

    private val _accessToken = MutableStateFlow(accessToken)
    override val accessToken: Flow<String?> = _accessToken

    override suspend fun setAccessToken(accessToken: String?) {
        _accessToken.value = accessToken
    }

    private val _userDetails = MutableStateFlow(userDetailsJson)
    override val userDetails: Flow<String?> = _userDetails

    override suspend fun setUserDetails(userDetailsJson: String?) {
        _userDetails.value = userDetailsJson
    }

    private val _loggedInAt = MutableStateFlow(loggedInAt)
    override val loggedInAt: Flow<String?> = _loggedInAt

    override suspend fun setLastLoggedIn(serializedDateTime: String?) {
        _loggedInAt.value = serializedDateTime
    }

    private val _lastLoggedInUsername = MutableStateFlow(lastLoggedInUsername)
    override val lastLoggedInUsername: Flow<String?> = _lastLoggedInUsername

    override suspend fun setLastLoggedInUsername(username: String?) {
        _lastLoggedInUsername.value = username
    }
}