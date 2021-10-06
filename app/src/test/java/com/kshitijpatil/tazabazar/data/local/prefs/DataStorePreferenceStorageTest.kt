package com.kshitijpatil.tazabazar.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DataStorePreferenceStorageTest {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferenceStorage: PreferenceStorage
    private val scope = TestCoroutineScope()

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(scope = scope) {
            ApplicationProvider
                .getApplicationContext<Context>()
                .preferencesDataStoreFile("test-tazabazar-prefs2")
        }
        preferenceStorage = DataStorePreferenceStorage(dataStore)
    }

    @After
    fun tearDown() {
        File(
            ApplicationProvider.getApplicationContext<Context>().filesDir,
            "datastore"
        ).deleteRecursively()
    }

    @Test
    fun storeAndGetRefreshToken() = scope.runBlockingTest {
        val refreshToken = "test-refresh-token"
        preferenceStorage.setRefreshToken(refreshToken)
        val reloaded = preferenceStorage.refreshToken.first()
        assertThat(reloaded).isEqualTo(refreshToken)
    }

    @Test
    fun storeAndGetAccessToken() = scope.runBlockingTest {
        val accessToken = "test-access-token"
        preferenceStorage.setAccessToken(accessToken)
        val reloaded = preferenceStorage.accessToken.first()
        assertThat(reloaded).isEqualTo(accessToken)
    }

    @Test
    fun storeAndGetUserDetails() = scope.runBlockingTest {
        val userDetails = "test-user-details"
        preferenceStorage.setUserDetails(userDetails)
        val reloaded = preferenceStorage.userDetails.first()
        assertThat(reloaded).isEqualTo(userDetails)
    }

    @Test
    fun storeAndGetLoggedInAt() = scope.runBlockingTest {
        val loggedInAt = "test-login-time"
        preferenceStorage.setLastLoggedIn(loggedInAt)
        val reloaded = preferenceStorage.loggedInAt.first()
        assertThat(reloaded).isEqualTo(loggedInAt)
    }
}