package com.kansion.musicplayer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object LyricsSearchService {

    suspend fun searchLyrics(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        val (parsedArtist, parsedTitle) = parseSongDetails(title, artist)
        if (parsedTitle.isEmpty()) return@withContext null

        // 1. Try LRCLIB first
        var lyrics = searchLrcLib(parsedArtist, parsedTitle)
        if (!lyrics.isNullOrBlank()) {
            return@withContext lyrics
        }

        // 2. Try Lyrist fallback (queries Genius/Musixmatch)
        lyrics = searchLyrist(parsedArtist, parsedTitle)
        if (!lyrics.isNullOrBlank()) {
            return@withContext lyrics
        }

        // 3. Try Lyrics.ovh fallback
        lyrics = searchLyricsOvh(parsedArtist, parsedTitle)
        if (!lyrics.isNullOrBlank()) {
            return@withContext lyrics
        }

        // 4. Try Lyrix fallback (specifically queries Musixmatch/Spotify OPM lyrics)
        lyrics = searchLyrix(parsedArtist, parsedTitle)
        if (!lyrics.isNullOrBlank()) {
            return@withContext lyrics
        }

        return@withContext null
    }

    private fun searchLrcLib(artist: String, title: String): String? {
        try {
            val query = "$artist $title".trim()
            if (query.isEmpty()) return null
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://lrclib.net/api/search?q=$encodedQuery"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonArray = JSONArray(response.toString())
                if (jsonArray.length() > 0) {
                    for (i in 0 until jsonArray.length()) {
                        val result = jsonArray.getJSONObject(i)
                        val plainLyrics = result.optString("plainLyrics")
                        if (!plainLyrics.isNullOrBlank() && plainLyrics != "null") {
                            return plainLyrics
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun searchLyrist(artist: String, title: String): String? {
        try {
            val encodedArtist = URLEncoder.encode(artist, "UTF-8")
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val urlString = "https://lyrist.vercel.app/api/$encodedTitle/$encodedArtist"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonObject = JSONObject(response.toString())
                val lyrics = jsonObject.optString("lyrics")
                if (!lyrics.isNullOrBlank() && lyrics != "null") {
                    return lyrics
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun searchLyricsOvh(artist: String, title: String): String? {
        try {
            val encodedArtist = URLEncoder.encode(artist, "UTF-8")
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val urlString = "https://api.lyrics.ovh/v1/$encodedArtist/$encodedTitle"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonObject = JSONObject(response.toString())
                val lyrics = jsonObject.optString("lyrics")
                if (!lyrics.isNullOrBlank() && lyrics != "null") {
                    return lyrics
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun searchLyrix(artist: String, title: String): String? {
        try {
            val encodedArtist = URLEncoder.encode(artist, "UTF-8")
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val urlString = "https://lyrix.vercel.app/getLyricsByName/$encodedArtist/$encodedTitle"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonObject = JSONObject(response.toString())
                val lyrics = jsonObject.optString("lyrics")
                if (!lyrics.isNullOrBlank() && lyrics != "null") {
                    return lyrics
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun parseSongDetails(title: String, artist: String): Pair<String, String> {
        // 1. Replace underscores with spaces
        val baseTitle = title.replace('_', ' ')
        val baseArtist = artist.replace('_', ' ')

        // 2. Clean brackets like (Official Video), [Lyrics], etc. and extensions from title
        val cleanTitle = baseTitle
            .replace(Regex("(?i)\\s*[\\[(](official|lyrics|video|audio|hd|lyric|remastered|cover|live|music\\s*video)[\\])]"), "")
            .replace(Regex("(?i)\\.mp3$|\\.wav$|\\.m4a$"), "")
            .trim()

        val isArtistUnknown = baseArtist.isBlank() || 
                baseArtist.equals("<unknown>", ignoreCase = true) || 
                baseArtist.equals("unknown", ignoreCase = true) || 
                baseArtist.equals("unknown artist", ignoreCase = true) || 
                baseArtist.equals("Di am-ammo nga Agkankansion", ignoreCase = true)

        val (rawArtist, rawTitle) = if (isArtistUnknown) {
            // Try parsing the title
            if (cleanTitle.contains(" - ")) {
                val parts = cleanTitle.split(" - ", limit = 2)
                Pair(parts[0].trim(), parts[1].trim())
            } else if (cleanTitle.contains(" by ", ignoreCase = true)) {
                val parts = cleanTitle.split(Regex("(?i)\\s+by\\s+"), 2)
                Pair(parts[1].trim(), parts[0].trim())
            } else {
                Pair("", cleanTitle)
            }
        } else {
            val cleanArtist = baseArtist
                .replace(Regex("(?i)\\s*[\\[(](official|video|audio|hd)[\\])]"), "")
                .trim()
            Pair(cleanArtist, cleanTitle)
        }

        // 3. Final Sanitize: Remove special characters and clean extra spacing
        val sanitizedArtist = rawArtist
            .replace(Regex("[~*+=/\\\\\"?!,@#%^&;:|]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val sanitizedTitle = rawTitle
            .replace(Regex("[~*+=/\\\\\"?!,@#%^&;:|]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return Pair(sanitizedArtist, sanitizedTitle)
    }
}
