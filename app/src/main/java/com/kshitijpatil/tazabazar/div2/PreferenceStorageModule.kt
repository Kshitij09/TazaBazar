package com.kshitijpatil.tazabazar.div2

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.kshitijpatil.tazabazar.base.SingletonHolder
import com.kshitijpatil.tazabazar.data.local.prefs.DataStorePreferenceStorage
import com.kshitijpatil.tazabazar.data.local.prefs.PreferenceStorage

interface PreferenceStorageModule {
    val preferenceStorage: PreferenceStorage
}

internal val Context.dataStore by preferencesDataStore(DataStorePreferenceStorage.PREFS_NAME)

class PreferenceStorageModuleImpl(appContext: Context) : PreferenceStorageModule {
    object PreferenceStorageHolder : SingletonHolder<PreferenceStorage, Context>({
        DataStorePreferenceStorage(it.dataStore)
    })

    override val preferenceStorage: PreferenceStorage =
        PreferenceStorageHolder.getInstance(appContext)
}