package com.kshitijpatil.tazabazar

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class TazaBazarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}