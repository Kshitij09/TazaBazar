package com.kshitijpatil.tazabazar.data.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kshitijpatil.tazabazar.api.dto.LoginResponse
import com.kshitijpatil.tazabazar.data.local.prefs.DataStorePreferenceStorage.PreferenceKeys.PREF_ACCESS_TOKEN
import com.kshitijpatil.tazabazar.data.local.prefs.DataStorePreferenceStorage.PreferenceKeys.PREF_LAST_LOGGED_IN_USERNAME
import com.kshitijpatil.tazabazar.data.local.prefs.DataStorePreferenceStorage.PreferenceKeys.PREF_LOGGED_IN_AT
import com.kshitijpatil.tazabazar.data.local.prefs.DataStorePreferenceStorage.PreferenceKeys.PREF_REFRESH_TOKEN
import com.kshitijpatil.tazabazar.data.local.prefs.DataStorePreferenceStorage.PreferenceKeys.PREF_USER_DETAILS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.threeten.bp.LocalDateTime

interface PreferenceStorage {
    suspend fun setRefreshToken(refreshToken: String?)
    val refreshToken: Flow<String?>

    suspend fun setAccessToken(accessToken: String?)
    val accessToken: Flow<String?>

    suspend fun setUserDetails(userDetailsJson: String?)

    /** Serialized instance of [LoginResponse] */
    val userDetails: Flow<String?>

    suspend fun setLastLoggedIn(serializedDateTime: String?)

    /**
     * Serialized [LocalDateTime] instance,
     * keeping timestamp of last successful login
     */
    val loggedInAt: Flow<String?>

    val lastLoggedInUsername: Flow<String?>
    suspend fun setLastLoggedInUsername(username: String?)
}

class DataStorePreferenceStorage(
    private val dataStore: DataStore<Preferences>
) : PreferenceStorage {
    companion object {
        const val PREFS_NAME = "tazabazar-prefs"
    }

    object PreferenceKeys {
        val PREF_REFRESH_TOKEN = stringPreferencesKey("pref_refresh_token")
        val PREF_ACCESS_TOKEN = stringPreferencesKey("pref_access_token")
        val PREF_USER_DETAILS = stringPreferencesKey("pref_user_details")
        val PREF_LOGGED_IN_AT = stringPreferencesKey("pref_logged_in_at")
        val PREF_LAST_LOGGED_IN_USERNAME = stringPreferencesKey("pref_last_logged_in_username")
    }

    override suspend fun setRefreshToken(refreshToken: String?) {
        dataStore.edit { it[PREF_REFRESH_TOKEN] = refreshToken ?: "" }
    }

    override val refreshToken: Flow<String?> =
        dataStore.data.map { mapEmptyToNull(it[PREF_REFRESH_TOKEN]) }

    override suspend fun setAccessToken(accessToken: String?) {
        dataStore.edit { it[PREF_ACCESS_TOKEN] = accessToken ?: "" }
    }

    override val accessToken: Flow<String?> =
        dataStore.data.map { mapEmptyToNull(it[PREF_ACCESS_TOKEN]) }

    override suspend fun setUserDetails(userDetailsJson: String?) {
        dataStore.edit { it[PREF_USER_DETAILS] = userDetailsJson ?: "" }
    }

    override val userDetails: Flow<String?> =
        dataStore.data.map { mapEmptyToNull(it[PREF_USER_DETAILS]) }

    override suspend fun setLastLoggedIn(serializedDateTime: String?) {
        dataStore.edit { it[PREF_LOGGED_IN_AT] = serializedDateTime ?: "" }
    }

    override val loggedInAt: Flow<String?> =
        dataStore.data.map { mapEmptyToNull(it[PREF_LOGGED_IN_AT]) }


    override val lastLoggedInUsername: Flow<String?> =
        dataStore.data.map { mapEmptyToNull(it[PREF_LAST_LOGGED_IN_USERNAME]) }

    override suspend fun setLastLoggedInUsername(username: String?) {
        dataStore.edit { it[PREF_LAST_LOGGED_IN_USERNAME] = username ?: "" }
    }

    private fun mapEmptyToNull(value: String?): String? {
        return if (value != null && value.isEmpty())
            null
        else
            value
    }
}