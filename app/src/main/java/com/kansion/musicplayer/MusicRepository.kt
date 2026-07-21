package com.kansion.musicplayer

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {

    private val dbHelper = MusicDatabaseHelper(context)

    // Load and scan all songs from MediaStore
    suspend fun getAllSongs(sortOrder: SortOrder = SortOrder.TITLE_ASC): List<Song> = withContext(Dispatchers.IO) {
        val songsList = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        
        val sortOrderString = when (sortOrder) {
            SortOrder.TITLE_ASC -> "${MediaStore.Audio.Media.TITLE} ASC"
            SortOrder.TITLE_DESC -> "${MediaStore.Audio.Media.TITLE} DESC"
            SortOrder.ARTIST_ASC -> "${MediaStore.Audio.Media.ARTIST} ASC"
            SortOrder.DATE_ADDED_DESC -> "${MediaStore.Audio.Media.DATE_ADDED} DESC"
            SortOrder.DURATION_DESC -> "${MediaStore.Audio.Media.DURATION} DESC"
        }
        
        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrderString
            )
            
            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                
                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val title = c.getString(titleCol) ?: "Kanta"
                    val artist = c.getString(artistCol) ?: "<unknown>"
                    val album = c.getString(albumCol) ?: "<unknown>"
                    val path = c.getString(dataCol) ?: ""
                    val duration = c.getLong(durationCol)
                    val size = c.getLong(sizeCol)
                    
                    // Filter out tracks with extremely short durations (less than 5s, usually UI click sounds or notifications)
                    if (duration > 5000) {
                        songsList.add(
                            Song(
                                id = id,
                                title = title,
                                artist = if (artist == MediaStore.UNKNOWN_STRING) "Di am-ammo nga Agkankansion" else artist,
                                album = if (album == MediaStore.UNKNOWN_STRING) "Album" else album,
                                path = path,
                                duration = duration,
                                size = size
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return@withContext songsList
    }

    // Playlists Operations
    fun getPlaylists(): List<Playlist> {
        return dbHelper.getPlaylists()
    }

    fun createPlaylist(name: String): Long {
        return dbHelper.createPlaylist(name)
    }

    fun deletePlaylist(playlistId: Long) {
        dbHelper.deletePlaylist(playlistId)
    }

    // Get songs mapped to a playlist ID
    suspend fun getSongsForPlaylist(playlistId: Long, allSongs: List<Song>): List<Song> = withContext(Dispatchers.IO) {
        val songIds = dbHelper.getSongsInPlaylist(playlistId).toSet()
        return@withContext allSongs.filter { it.id in songIds }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long): Boolean {
        return dbHelper.addSongToPlaylist(playlistId, songId)
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        dbHelper.removeSongFromPlaylist(playlistId, songId)
    }

    // Persistent Queue Operations
    fun saveQueue(songs: List<Song>) {
        dbHelper.saveQueue(songs.map { it.id })
    }

    fun getSavedQueue(allSongs: List<Song>): List<Song> {
        val songIds = dbHelper.getSavedQueue()
        val songsMap = allSongs.associateBy { it.id }
        return songIds.mapNotNull { songsMap[it] }
    }

    suspend fun getLyrics(songId: Long): String? = withContext(Dispatchers.IO) {
        return@withContext dbHelper.getLyrics(songId)
    }

    suspend fun saveLyrics(songId: Long, lyricsText: String) = withContext(Dispatchers.IO) {
        dbHelper.saveLyrics(songId, lyricsText)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dbHelper.clearAllData()
    }
}
