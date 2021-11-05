package com.kshitijpatil.tazabazar.div2

import android.content.Context
import com.kshitijpatil.tazabazar.BuildConfig
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope

interface AppComponent {
    val coroutineScope: CoroutineScope
    val dispatchers: AppCoroutineDispatchers
    val okHttpModule: OkHttpModule
    val databaseModule: DatabaseModule
    val preferenceStorageModule: PreferenceStorageModule
}

class AppComponentImpl(private val appContext: Context) : AppComponent {
    override val coroutineScope = MainScope()
    override val dispatchers = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main
    )
    override val okHttpModule: OkHttpModule
        get() {
            return if (BuildConfig.DEBUG)
                DebugOkHttpClientModuleImpl(appContext)
            else
                ProdOkhttpClientModuleImpl(appContext)
        }
    override val databaseModule: DatabaseModule
        get() = DatabaseModuleImpl(appContext)

    override val preferenceStorageModule: PreferenceStorageModule
        get() = PreferenceStorageModuleImpl(appContext)
}
