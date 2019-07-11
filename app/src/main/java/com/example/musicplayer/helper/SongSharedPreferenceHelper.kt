package com.example.musicplayer.helper

import android.content.Context
import com.example.musicplayer.R

class SongSharedPreferenceHelper(private val context: Context) {
    private val repeatType = "repeatType"
    private val shuffleType = "shuffleType"
    private val repeatKey = "repeatKey"
    private val shuffleKey = "shuffleKey"

    fun saveRepeatType(type: String) {
        val pref = context.getSharedPreferences(repeatType, Context.MODE_PRIVATE)
        pref.edit().putString(repeatKey, type).apply()
    }

    fun saveShuffleType(type: String) {
        val pref = context.getSharedPreferences(shuffleType, Context.MODE_PRIVATE)
        pref.edit().putString(shuffleKey, type).apply()
    }

    fun loadRepeatType(): String {
        val pref = context.getSharedPreferences(repeatType, Context.MODE_PRIVATE)
        val default = context.getString(R.string.repeat_off)
        return pref.getString(repeatKey, default) ?: default
    }

    fun loadShuffleType(): String {
        val pref = context.getSharedPreferences(shuffleType, Context.MODE_PRIVATE)
        val default = context.getString(R.string.shuffle_off)
        return pref.getString(shuffleKey, default) ?: default
    }
}
