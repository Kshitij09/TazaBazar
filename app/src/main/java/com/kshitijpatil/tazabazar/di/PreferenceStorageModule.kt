package com.kshitijpatil.tazabazar.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.kshitijpatil.tazabazar.base.SingletonHolder
import com.kshitijpatil.tazabazar.data.local.prefs.DataStorePreferenceStorage
import com.kshitijpatil.tazabazar.data.local.prefs.PreferenceStorage

object PreferenceStorageModule {
    val Context.dataStore by preferencesDataStore(DataStorePreferenceStorage.PREFS_NAME)

    object PreferenceStorageHolder : SingletonHolder<PreferenceStorage, Context>({
        DataStorePreferenceStorage(it.dataStore)
    })

    fun providePreferenceStorage(context: Context): PreferenceStorage {
        return PreferenceStorageHolder.getInstance(context)
    }
}
