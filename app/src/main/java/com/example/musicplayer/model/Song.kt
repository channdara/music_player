package com.example.musicplayer.model

class Song(
    val path: String = "",
    val title: String = "",
    val album: String = "",
    val artist: String = "",
    var isPlaying: Boolean = false
) {
    fun getArtistAndAlbum(artist: String, album: String): String {
        return "Artist: $artist - Album: $album"
    }
}