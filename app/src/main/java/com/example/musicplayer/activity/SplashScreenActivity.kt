package com.example.musicplayer.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.musicplayer.R

class SplashScreenActivity : AppCompatActivity() {

    private val img by lazy { findViewById<ImageView>(R.id.iv_splash_screen) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Glide.with(this).load(R.drawable.gif_splash).into(img)
        Handler().postDelayed({
            startActivity(Intent(this, SongActivity::class.java))
            finish()
        }, 2000)
    }
}
