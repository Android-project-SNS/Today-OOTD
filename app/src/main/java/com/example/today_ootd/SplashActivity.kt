package com.example.today_ootd

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.window.SplashScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Keep the splash screen visible for this Activity
        splashScreen.setKeepOnScreenCondition { true }
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}