package com.kansion.musicplayer

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val path: String,
    val duration: Long, // in milliseconds
    val size: Long
) {
    val uri: Uri
        get() = Uri.parse("content://media/external/audio/media/$id")
}

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0
)

enum class SortOrder {
    TITLE_ASC,
    TITLE_DESC,
    ARTIST_ASC,
    DATE_ADDED_DESC,
    DURATION_DESC
}

enum class RepeatMode {
    NONE,
    ALL,
    ONE
}
