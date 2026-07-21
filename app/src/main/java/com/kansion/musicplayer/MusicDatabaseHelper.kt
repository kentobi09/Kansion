package com.kansion.musicplayer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MusicDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "kansion.db"
        private const val DATABASE_VERSION = 1

        // Playlists Table
        private const val TABLE_PLAYLISTS = "playlists"
        private const val KEY_PLAYLIST_ID = "id"
        private const val KEY_PLAYLIST_NAME = "name"

        // Playlist Songs Mapping Table
        private const val TABLE_PLAYLIST_SONGS = "playlist_songs"
        private const val KEY_MAPPING_PLAYLIST_ID = "playlist_id"
        private const val KEY_MAPPING_SONG_ID = "song_id"

        // Persistent Queue Table
        private const val TABLE_QUEUE = "queue"
        private const val KEY_QUEUE_SONG_ID = "song_id"
        private const val KEY_QUEUE_ORDER = "queue_order"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createPlaylistsTable = ("CREATE TABLE " + TABLE_PLAYLISTS + "("
                + KEY_PLAYLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PLAYLIST_NAME + " TEXT UNIQUE NOT NULL" + ")")
        
        val createPlaylistSongsTable = ("CREATE TABLE " + TABLE_PLAYLIST_SONGS + "("
                + KEY_MAPPING_PLAYLIST_ID + " INTEGER,"
                + KEY_MAPPING_SONG_ID + " INTEGER,"
                + "PRIMARY KEY (" + KEY_MAPPING_PLAYLIST_ID + ", " + KEY_MAPPING_SONG_ID + "),"
                + "FOREIGN KEY(" + KEY_MAPPING_PLAYLIST_ID + ") REFERENCES " + TABLE_PLAYLISTS + "(" + KEY_PLAYLIST_ID + ") ON DELETE CASCADE" + ")")

        val createQueueTable = ("CREATE TABLE " + TABLE_QUEUE + "("
                + KEY_QUEUE_SONG_ID + " INTEGER PRIMARY KEY,"
                + KEY_QUEUE_ORDER + " INTEGER" + ")")

        db.execSQL(createPlaylistsTable)
        db.execSQL(createPlaylistSongsTable)
        db.execSQL(createQueueTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYLIST_SONGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYLISTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUEUE")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Playlists Operations
    fun createPlaylist(name: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_PLAYLIST_NAME, name)
        }
        return try {
            db.insert(TABLE_PLAYLISTS, null, values)
        } catch (e: Exception) {
            -1L
        }
    }

    fun deletePlaylist(playlistId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_PLAYLISTS, "$KEY_PLAYLIST_ID = ?", arrayOf(playlistId.toString()))
    }

    fun getPlaylists(): List<Playlist> {
        val playlists = mutableListOf<Playlist>()
        val db = this.readableDatabase
        val query = "SELECT p.*, COUNT(ps.song_id) as song_count FROM $TABLE_PLAYLISTS p LEFT JOIN $TABLE_PLAYLIST_SONGS ps ON p.$KEY_PLAYLIST_ID = ps.$KEY_MAPPING_PLAYLIST_ID GROUP BY p.$KEY_PLAYLIST_ID"
        val cursor = db.rawQuery(query, null)
        
        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(KEY_PLAYLIST_ID)
            val nameIndex = cursor.getColumnIndex(KEY_PLAYLIST_NAME)
            val countIndex = cursor.getColumnIndex("song_count")
            
            do {
                if (idIndex != -1 && nameIndex != -1 && countIndex != -1) {
                    playlists.add(
                        Playlist(
                            id = cursor.getLong(idIndex),
                            name = cursor.getString(nameIndex),
                            songCount = cursor.getInt(countIndex)
                        )
                    )
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return playlists
    }

    // Playlist Songs Operations
    fun addSongToPlaylist(playlistId: Long, songId: Long): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_MAPPING_PLAYLIST_ID, playlistId)
            put(KEY_MAPPING_SONG_ID, songId)
        }
        return try {
            db.insertWithOnConflict(TABLE_PLAYLIST_SONGS, null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_PLAYLIST_SONGS, "$KEY_MAPPING_PLAYLIST_ID = ? AND $KEY_MAPPING_SONG_ID = ?", arrayOf(playlistId.toString(), songId.toString()))
    }

    fun getSongsInPlaylist(playlistId: Long): List<Long> {
        val songIds = mutableListOf<Long>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_PLAYLIST_SONGS,
            arrayOf(KEY_MAPPING_SONG_ID),
            "$KEY_MAPPING_PLAYLIST_ID = ?",
            arrayOf(playlistId.toString()),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(KEY_MAPPING_SONG_ID)
            if (index != -1) {
                do {
                    songIds.add(cursor.getLong(index))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return songIds
    }

    // Queue Operations
    fun saveQueue(songIds: List<Long>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_QUEUE, null, null)
            songIds.forEachIndexed { index, songId ->
                val values = ContentValues().apply {
                    put(KEY_QUEUE_SONG_ID, songId)
                    put(KEY_QUEUE_ORDER, index)
                }
                db.insert(TABLE_QUEUE, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getSavedQueue(): List<Long> {
        val songIds = mutableListOf<Long>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_QUEUE,
            arrayOf(KEY_QUEUE_SONG_ID),
            null, null, null, null,
            "$KEY_QUEUE_ORDER ASC"
        )
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(KEY_QUEUE_SONG_ID)
            if (index != -1) {
                do {
                    songIds.add(cursor.getLong(index))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return songIds
    }

    fun clearAllData() {
        val db = this.writableDatabase
        db.delete(TABLE_PLAYLIST_SONGS, null, null)
        db.delete(TABLE_PLAYLISTS, null, null)
        db.delete(TABLE_QUEUE, null, null)
        db.close()
    }
}
