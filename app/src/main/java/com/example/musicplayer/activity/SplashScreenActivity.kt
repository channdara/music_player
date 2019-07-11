package com.example.musicplayer.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.example.musicplayer.R
import com.example.musicplayer.util.PermissionUtil

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        PermissionUtil.requestPhonePermissions(this)
        if (PermissionUtil.isPhonePermissionGranted(this)) {
            startTimer()
            return
        }
        checkAndRequestPermissions()
    }

    private fun startTimer() {
        Handler().postDelayed({
            startActivity(Intent(this, SongActivity::class.java))
            finish()
        }, 1000)
    }
}
