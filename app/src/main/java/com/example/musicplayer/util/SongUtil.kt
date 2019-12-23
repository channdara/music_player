package com.example.musicplayer.util

import android.content.Context
import android.media.MediaPlayer
import android.provider.MediaStore
import android.support.v7.widget.AppCompatImageView
import android.widget.SeekBar
import android.widget.TextView
import com.example.musicplayer.R
import com.example.musicplayer.helper.SongSharedPreferenceHelper
import com.example.musicplayer.model.Song
import java.util.concurrent.TimeUnit

data class SongData(val songList: ArrayList<Song>, val errorMessage: String)

class SongUtil(private val context: Context, private val pref: SongSharedPreferenceHelper) {
    val mediaPlayer: MediaPlayer = MediaPlayer()

    fun fetchLocalSong(): SongData {
        val songs: ArrayList<Song> = ArrayList()
        try {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
            val cursor = context.contentResolver.query(uri, null, selection, null, sortOrder)
            if (cursor != null && cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    songs.add(Song(path, title, album, artist))
                }
                cursor.close()
            }
        } catch (e: Exception) {
            return SongData(songs, e.message.toString())
        }
        return SongData(songs, "")
    }

    fun start(path: String) = mediaPlayer.apply {
        reset()
        setDataSource(path)
        prepare()
        start()
    }

    fun stop() = mediaPlayer.stop()

    fun toTime(value: Long): String {
        val h = TimeUnit.MILLISECONDS.toHours(value)
        val m = TimeUnit.MILLISECONDS.toMinutes(value)
        val s = TimeUnit.MILLISECONDS.toSeconds(value) % 60
        var (hour, minute, second) = arrayOf(h.toString(), m.toString(), s.toString())
        if (h < 10) hour = "0$h"
        if (m < 10) minute = "0$m"
        if (s < 10) second = "0$s"
        return "$hour:$minute:$second"
    }

    fun setupPlayAndPause(
        imageView1: AppCompatImageView,
        imageView2: AppCompatImageView,
        contentDescription: Int,
        imageDrawable: Int,
        background: Int
    ) {
        imageView1.contentDescription = getString(contentDescription)
        imageView1.background = getDrawable(background)
        imageView1.setImageDrawable(getDrawable(imageDrawable))
        imageView2.contentDescription = getString(contentDescription)
        imageView2.background = getDrawable(background)
        imageView2.setImageDrawable(getDrawable(imageDrawable))
    }

    fun setupTitleArtistAndAlbum(
        textViewTitle1: TextView,
        textViewTitle2: TextView,
        textViewArtistAndAlbum1: TextView,
        textViewArtistAndAlbum2: TextView,
        song: Song?
    ) {
        if (song != null) {
            textViewTitle1.text = song.title
            textViewTitle2.text = song.title
            textViewArtistAndAlbum1.text = song.getArtistAndAlbum(song.artist, song.album)
            textViewArtistAndAlbum2.text = song.getArtistAndAlbum(song.artist, song.album)
            return
        }
        val selectSongToPlay = getString(R.string.select_song_to_play)
        textViewTitle1.text = selectSongToPlay
        textViewTitle2.text = selectSongToPlay
        textViewArtistAndAlbum1.text = selectSongToPlay
        textViewArtistAndAlbum2.text = selectSongToPlay
    }

    fun setupShuffleOrRepeat(imageView: AppCompatImageView, contentDescription: Int, background: Int) {
        imageView.contentDescription = getString(contentDescription)
        imageView.background = getDrawable(background)
    }

    fun togglePlayAndPause(imageView1: AppCompatImageView, imageView2: AppCompatImageView, seekBar: SeekBar) {
        when (imageView1.contentDescription.toString()) {
            getString(R.string.play) -> {
                setupPlayAndPause(
                    imageView1,
                    imageView2,
                    R.string.pause,
                    R.drawable.ic_play_circle,
                    R.drawable.song_circle_image_green
                )
                pause()
                seekBar.progress = mediaPlayer.currentPosition
            }
            getString(R.string.pause) -> {
                setupPlayAndPause(
                    imageView1,
                    imageView2,
                    R.string.play,
                    R.drawable.ic_pause_circle,
                    R.drawable.song_circle_image_orange
                )
                resume(mediaPlayer.currentPosition)
            }
        }
    }

    fun toggleShuffleAndSaveSharedPreference(imageView: AppCompatImageView) {
        when (imageView.contentDescription.toString()) {
            getString(R.string.shuffle_off) -> {
                setupShuffleOrRepeat(imageView, R.string.shuffle_on, R.drawable.song_circle_image_green)
                pref.saveShuffleType(getString(R.string.shuffle_on))
            }
            getString(R.string.shuffle_on) -> {
                setupShuffleOrRepeat(imageView, R.string.shuffle_off, R.drawable.song_circle_image_red)
                pref.saveShuffleType(getString(R.string.shuffle_off))
            }
        }
    }

    fun toggleRepeatAndSaveSharedPreference(imageView: AppCompatImageView) {
        when (imageView.contentDescription.toString()) {
            getString(R.string.repeat_off) -> {
                setupShuffleOrRepeat(imageView, R.string.repeat_one, R.drawable.song_circle_image_orange)
                pref.saveRepeatType(getString(R.string.repeat_one))
            }
            getString(R.string.repeat_one) -> {
                setupShuffleOrRepeat(imageView, R.string.repeat_all, R.drawable.song_circle_image_green)
                pref.saveRepeatType(getString(R.string.repeat_all))
            }
            getString(R.string.repeat_all) -> {
                setupShuffleOrRepeat(imageView, R.string.repeat_off, R.drawable.song_circle_image_red)
                pref.saveRepeatType(getString(R.string.repeat_off))
            }
        }
    }

    private fun pause() = mediaPlayer.pause()

    private fun resume(currentPosition: Int) = mediaPlayer.apply {
        seekTo(currentPosition)
        start()
    }

    private fun getString(value: Int) = context.getString(value)

    private fun getDrawable(value: Int) = context.getDrawable(value)
}