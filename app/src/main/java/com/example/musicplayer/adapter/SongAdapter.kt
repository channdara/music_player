package com.example.musicplayer.adapter

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.musicplayer.R
import com.example.musicplayer.listener.RecyclerViewListener
import com.example.musicplayer.model.Song

class SongAdapter(
    private val songList: ArrayList<Song>,
    private val listener: RecyclerViewListener
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item_view, parent, false)
        return SongViewHolder(parent.context, view)
    }

    override fun getItemCount(): Int = songList.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) = holder.bind(holder.adapterPosition)

    inner class SongViewHolder(private val context: Context, view: View) : RecyclerView.ViewHolder(view) {
        private val tvSongTitle: TextView = view.findViewById(R.id.tv_rcv_song_title)
        private val tvSongArtistAndAlbum: TextView = view.findViewById(R.id.tv_rcv_artist_and_album)

        fun bind(index: Int) {
            val song = songList[index]
            tvSongTitle.text = song.title
            tvSongArtistAndAlbum.text = song.getArtistAndAlbum(song.artist, song.album)
            if (song.isPlaying) {
                itemView.setBackgroundColor(context.getColor(R.color.color_primary))
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }
            itemView.setOnClickListener {
                listener.onItemClick(index)
                notifyItemChanged(index)
            }
        }
    }
}