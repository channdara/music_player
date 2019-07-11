package com.example.musicplayer.activity

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.helper.SongSharedPreferenceHelper
import com.example.musicplayer.listener.RecyclerViewListener
import com.example.musicplayer.model.Song
import com.example.musicplayer.util.SongUtil

class SongActivity : AppCompatActivity() {
    private lateinit var songAdapter: SongAdapter

    private val pref by lazy { SongSharedPreferenceHelper(this) }
    private val songUtil by lazy { SongUtil(this, pref) }

    private val rcvSong by lazy { findViewById<RecyclerView>(R.id.recycler_view) }
    private val sbSong by lazy { findViewById<SeekBar>(R.id.seek_bar_song) }
    private val sbVolume by lazy { findViewById<SeekBar>(R.id.seek_bar_volume) }
    private val layoutMiniPlay by lazy { findViewById<LinearLayout>(R.id.layout_mini_play) }
    private val layoutBottomSheet by lazy { findViewById<LinearLayout>(R.id.layout_bottom_sheet) }
    private val layoutDragView by lazy { findViewById<LinearLayout>(R.id.layout_drag_view) }
    private val imgArrowDown by lazy { findViewById<AppCompatImageView>(R.id.img_mini_play_arrow_down) }
    private val imgMiniPrevious by lazy { findViewById<AppCompatImageView>(R.id.img_mini_song_previous) }
    private val imgMiniPlayAndPause by lazy { findViewById<AppCompatImageView>(R.id.img_mini_song_play_and_pause) }
    private val imgMiniNext by lazy { findViewById<AppCompatImageView>(R.id.img_mini_song_next) }
    private val imgSongShuffle by lazy { findViewById<AppCompatImageView>(R.id.img_song_shuffle) }
    private val imgSongPrevious by lazy { findViewById<AppCompatImageView>(R.id.img_song_previous) }
    private val imgSongPlayAndPause by lazy { findViewById<AppCompatImageView>(R.id.img_song_play_and_pause) }
    private val imgSongNext by lazy { findViewById<AppCompatImageView>(R.id.img_song_next) }
    private val imgSongRepeat by lazy { findViewById<AppCompatImageView>(R.id.img_song_repeat) }
    private val tvErrorMessage by lazy { findViewById<TextView>(R.id.tv_error_message) }
    private val tvMiniSongTitle by lazy { findViewById<TextView>(R.id.tv_mini_song_title) }
    private val tvMiniSongAlbumAndArtist by lazy { findViewById<TextView>(R.id.tv_mini_song_album_and_artist) }
    private val tvSongDurationCounting by lazy { findViewById<TextView>(R.id.tv_song_duration_counting) }
    private val tvSongMaxDuration by lazy { findViewById<TextView>(R.id.tv_song_max_duration) }
    private val tvSongTitle by lazy { findViewById<TextView>(R.id.tv_song_title) }
    private val tvSongAlbumAndArtist by lazy { findViewById<TextView>(R.id.tv_song_album_and_artist) }
    private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(layoutBottomSheet) }

    private var errorMessage: String = ""
    private var songList: ArrayList<Song> = ArrayList()
    private var previousIndex: Int = -1
    private var songShuffleContentDes: String = ""
    private var songRepeatContentDes: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)
        loadSongs()
        loadSharedPreferences()
        setupRecyclerView()
        setupBottomSheetListener()
        setupShuffleAndRepeat()
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }
        super.onBackPressed()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) return super.onKeyDown(keyCode, event)
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            sbVolume.progress = sbVolume.progress + 1
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            sbVolume.progress = sbVolume.progress - 1
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun loadSongs() {
        val result = songUtil.fetchLocalSong()
        songList = result.songList
        errorMessage = result.errorMessage
    }

    private fun loadSharedPreferences() {
        songShuffleContentDes = pref.loadShuffleType()
        songRepeatContentDes = pref.loadRepeatType()
    }

    private fun setupRecyclerView() {
        if (errorMessage.isNotEmpty()) {
            rcvSong.visibility = View.GONE
            tvErrorMessage.visibility = View.VISIBLE
            tvErrorMessage.text = errorMessage
            return
        }
        if (songList.isEmpty()) {
            rcvSong.visibility = View.GONE
            tvErrorMessage.visibility = View.VISIBLE
            return
        }
        songAdapter = SongAdapter(songList, object : RecyclerViewListener {
            override fun onItemClick(index: Int) {
                startSong(index)
                setupBottomSheet(songList[index])
                setupSongSeekBar()
            }
        })
        rcvSong.layoutManager = LinearLayoutManager(this)
        rcvSong.adapter = songAdapter
    }

    private fun startSong(index: Int) {
        val song = songList[index]
        if (previousIndex != -1 && previousIndex != index) {
            songList[previousIndex].isPlaying = false
            songAdapter.notifyItemChanged(previousIndex)
        }
        previousIndex = index
        if (song.isPlaying) {
            song.isPlaying = false
            songUtil.stop()
            return
        }
        songUtil.start(song.path)
        sbSong.max = songUtil.mediaPlayer.duration
        song.isPlaying = true
        onSongComplete(song)
    }

    private fun onSongComplete(song: Song) {
        songUtil.mediaPlayer.setOnCompletionListener {
            when (imgSongRepeat.contentDescription.toString()) {
                getString(R.string.repeat_off) -> {
                    song.isPlaying = false
                    songAdapter.notifyItemChanged(previousIndex)
                    setupBottomSheet(songList[previousIndex])
                    if (previousIndex == songList.size - 1) return@setOnCompletionListener
                    previousIndex++
                    startSong(previousIndex)
                    songAdapter.notifyItemChanged(previousIndex)
                    setupBottomSheet(songList[previousIndex])
                }
                getString(R.string.repeat_one) -> {
                    songUtil.start(songList[previousIndex].path)
                }
                getString(R.string.repeat_all) -> {
                    song.isPlaying = false
                    songAdapter.notifyItemChanged(previousIndex)
                    if (previousIndex == songList.size - 1) {
                        setupBottomSheet(songList[previousIndex])
                        startSong(0)
                        songAdapter.notifyItemChanged(previousIndex)
                        setupBottomSheet(songList[previousIndex])
                        return@setOnCompletionListener
                    }
                    previousIndex++
                    startSong(previousIndex)
                    songAdapter.notifyItemChanged(previousIndex)
                    setupBottomSheet(songList[previousIndex])
                }
            }
        }
    }

    private fun setupBottomSheet(song: Song) {
        if (song.isPlaying) {
            songUtil.setupPlayAndPause(
                imgMiniPlayAndPause,
                imgSongPlayAndPause,
                R.string.play,
                R.drawable.ic_pause_circle,
                R.drawable.song_circle_image_orange
            )
            songUtil.setupTitleArtistAndAlbum(
                tvSongTitle,
                tvMiniSongTitle,
                tvSongAlbumAndArtist,
                tvMiniSongAlbumAndArtist,
                song
            )
            tvSongMaxDuration.text = songUtil.toTime(songUtil.mediaPlayer.duration.toLong())
            return
        }
        songUtil.setupPlayAndPause(
            imgMiniPlayAndPause,
            imgSongPlayAndPause,
            R.string.stop,
            R.drawable.ic_stop_circle,
            R.drawable.song_circle_image_red
        )
        songUtil.setupTitleArtistAndAlbum(
            tvSongTitle,
            tvMiniSongTitle,
            tvSongAlbumAndArtist,
            tvMiniSongAlbumAndArtist,
            null
        )
        tvSongMaxDuration.text = getString(R.string.default_song_duration)
    }

    private fun setupBottomSheetListener() {
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onSlide(view: View, offSet: Float) {
                if (offSet > 0.5) {
                    setupVolumeSeekBar()
                    layoutMiniPlay.visibility = View.GONE
                    imgArrowDown.visibility = View.VISIBLE
                    return
                }
                layoutMiniPlay.visibility = View.VISIBLE
                imgArrowDown.visibility = View.GONE
            }

            override fun onStateChanged(view: View, state: Int) {}
        })
        layoutDragView.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        imgArrowDown.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        imgMiniPlayAndPause.setOnClickListener {
            songUtil.togglePlayAndPause(imgMiniPlayAndPause, imgSongPlayAndPause, sbSong)
        }
        imgSongPlayAndPause.setOnClickListener {
            songUtil.togglePlayAndPause(imgSongPlayAndPause, imgMiniPlayAndPause, sbSong)
        }
        imgSongShuffle.setOnClickListener {
            songUtil.toggleShuffleAndSaveSharedPreference(imgSongShuffle)
        }
        imgSongRepeat.setOnClickListener {
            songUtil.toggleRepeatAndSaveSharedPreference(imgSongRepeat)
        }
    }

    private fun setupSongSeekBar() {
        val handler = Handler()
        val postDelayed: Long = 500
        val seekBarRunnable: Runnable = object : Runnable {
            override fun run() {
                tvSongDurationCounting.text = songUtil.toTime(songUtil.mediaPlayer.currentPosition.toLong())
                sbSong.progress = songUtil.mediaPlayer.currentPosition
                handler.postDelayed(this, postDelayed)
            }
        }
        handler.postDelayed(seekBarRunnable, postDelayed)
        sbSong.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (imgSongPlayAndPause.contentDescription != getString(R.string.stop)) return
                tvSongDurationCounting.text = getString(R.string.default_song_duration)
                seekBar?.progress = 0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                songUtil.mediaPlayer.seekTo(seekBar?.progress ?: 0)
            }
        })
    }

    private fun setupVolumeSeekBar() {
        volumeControlStream = AudioManager.STREAM_MUSIC
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sbVolume.max = audioManager.getStreamMaxVolume(volumeControlStream)
        sbVolume.progress = audioManager.getStreamVolume(volumeControlStream)
        sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(volumeControlStream, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupShuffleAndRepeat() {
        when (songShuffleContentDes) {
            getString(R.string.shuffle_on) ->
                songUtil.setupShuffleOrRepeat(imgSongShuffle, R.string.shuffle_on, R.drawable.song_circle_image_green)
            getString(R.string.shuffle_off) ->
                songUtil.setupShuffleOrRepeat(imgSongShuffle, R.string.shuffle_off, R.drawable.song_circle_image_red)
        }
        when (songRepeatContentDes) {
            getString(R.string.repeat_off) ->
                songUtil.setupShuffleOrRepeat(imgSongRepeat, R.string.repeat_off, R.drawable.song_circle_image_red)
            getString(R.string.repeat_one) ->
                songUtil.setupShuffleOrRepeat(imgSongRepeat, R.string.repeat_one, R.drawable.song_circle_image_orange)
            getString(R.string.repeat_all) ->
                songUtil.setupShuffleOrRepeat(imgSongRepeat, R.string.repeat_all, R.drawable.song_circle_image_green)
        }
    }
}
