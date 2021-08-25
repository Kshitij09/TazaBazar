package com.kshitijpatil.tazabazar.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kshitijpatil.tazabazar.MainActivity
import com.kshitijpatil.tazabazar.R
import kotlinx.coroutines.delay

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val mainIntent = Intent(this@SplashActivity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        lifecycleScope.launchWhenCreated {
            delay(SPLASH_DURATION_IN_MILLIS)
            startActivity(mainIntent)
            finish()
        }
    }

    companion object {
        private const val SPLASH_DURATION_IN_MILLIS = 3000L
    }
}