package com.audiovideoplayer.sinima.data

data class MediaItem(
    val id: Long,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val duration: Long = 0L,
    val uri: String,
    val albumArtUri: String? = null,
    val mimeType: String = ""
)
