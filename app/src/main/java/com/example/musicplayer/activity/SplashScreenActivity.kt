package com.example.musicplayer.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.util.PermissionUtil

class SplashScreenActivity : AppCompatActivity() {

    private val imageView by lazy { findViewById<AppCompatImageView>(R.id.iv_splash_screen) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        PermissionUtil.requestPhonePermissions(this)
        if (PermissionUtil.isPhonePermissionGranted(this)) {
            Glide.with(this).load(R.drawable.gif_splash).into(imageView)
            startTimer()
            return
        }
        checkAndRequestPermissions()
    }

    private fun startTimer() {
        Handler().postDelayed({
            startActivity(Intent(this, SongActivity::class.java))
            finish()
        }, 2000)
    }
}
