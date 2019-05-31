package com.example.musicplayer.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.listener.RecyclerViewListener
import com.example.musicplayer.model.Song
import com.example.musicplayer.util.PermissionUtil

class SongActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var textViewErrorMessage: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var adapter: SongAdapter

    private var errorMessage: String = ""
    private var previousIndex: Int = -1

    private val postDelayed: Long = 500
    private val songList: ArrayList<Song> = ArrayList()
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val handler: Handler = Handler()
    private val seekBarRunnable: Runnable = object : Runnable {
        override fun run() {
            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(this, postDelayed)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)
        initComponents()
        checkAndRequestPermissions()
    }

    private fun initComponents() {
        recyclerView = findViewById(R.id.recycler_view)
        textViewErrorMessage = findViewById(R.id.tv_error_message)
        seekBar = findViewById(R.id.seek_bar)
    }

    private fun checkAndRequestPermissions() {
        PermissionUtil.requestPhonePermissions(this)
        if (PermissionUtil.isPhonePermissionGranted(this)) {
            fetchSong()
            setupRecyclerView()
            setupSeekBar()
            return
        }
        checkAndRequestPermissions()
    }

    private fun fetchSong() {
        try {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
            val cursor = contentResolver.query(uri, null, selection, null, sortOrder)
            if (cursor != null && cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    songList.add(Song(path, title, album, artist))
                }
                cursor.close()
            }
        } catch (e: Exception) {
            this.errorMessage = e.message.toString()
        }
    }

    private fun setupRecyclerView() {
        if (errorMessage.isNotEmpty()) {
            recyclerView.visibility = View.GONE
            textViewErrorMessage.visibility = View.VISIBLE
            textViewErrorMessage.text = errorMessage
            return
        }
        if (songList.isEmpty()) {
            recyclerView.visibility = View.GONE
            textViewErrorMessage.visibility = View.VISIBLE
            return
        }
        adapter = SongAdapter(songList, object : RecyclerViewListener {
            override fun onItemClick(index: Int) {
                playSong(index)
                seekBar.max = mediaPlayer.duration
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun playSong(index: Int) {
        val song = songList[index]
        mediaPlayer.stop()
        if (previousIndex != -1 && previousIndex != index) {
            songList[previousIndex].isPlaying = false
            adapter.notifyItemChanged(previousIndex)
        }
        previousIndex = index
        if (song.isPlaying) {
            song.isPlaying = false
            return
        }
        play(song.path)
        song.isPlaying = true
        mediaPlayer.setOnCompletionListener {
            song.isPlaying = false
            adapter.notifyItemChanged(previousIndex)
            if (previousIndex == songList.size - 1) return@setOnCompletionListener
            previousIndex++
            adapter.notifyItemChanged(previousIndex)
            playSong(previousIndex)
        }
    }

    private fun play(path: String) {
        mediaPlayer.apply {
            reset()
            setDataSource(path)
            prepare()
            start()
        }
    }

    private fun setupSeekBar() {
        handler.postDelayed(seekBarRunnable, postDelayed)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar?.let {
                    if (mediaPlayer.isPlaying) return
                    it.progress = 0
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mediaPlayer.seekTo(it.progress)
                }
            }
        })
    }
}
