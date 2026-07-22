package com.kansion.musicplayer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object LyricsSearchService {

    suspend fun searchLyrics(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        val query = cleanQueryString(title, artist)
        if (query.isEmpty()) return@withContext null

        try {
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
                    // Try to find the first result containing plain lyrics
                    for (i in 0 until jsonArray.length()) {
                        val result = jsonArray.getJSONObject(i)
                        val plainLyrics = result.optString("plainLyrics")
                        if (!plainLyrics.isNullOrBlank() && plainLyrics != "null") {
                            return@withContext plainLyrics
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    fun cleanQueryString(title: String, artist: String): String {
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

        val rawResult = if (isArtistUnknown) {
            // Try parsing the title
            if (cleanTitle.contains(" - ")) {
                val parts = cleanTitle.split(" - ", limit = 2)
                val parsedArtist = parts[0].trim()
                val parsedTitle = parts[1].trim()
                "$parsedArtist $parsedTitle"
            } else if (cleanTitle.contains(" by ", ignoreCase = true)) {
                val parts = cleanTitle.split(Regex("(?i)\\s+by\\s+"), 2)
                val parsedTitle = parts[0].trim()
                val parsedArtist = parts[1].trim()
                "$parsedArtist $parsedTitle"
            } else {
                cleanTitle
            }
        } else {
            val cleanArtist = baseArtist
                .replace(Regex("(?i)\\s*[\\[(](official|video|audio|hd)[\\])]"), "")
                .trim()
            "$cleanArtist $cleanTitle"
        }

        // 3. Final Sanitize: Remove special characters and clean extra spacing
        return rawResult
            .replace(Regex("[~*+=/\\\\\"?!,@#%^&;:|]"), " ") // Remove weird special chars
            .replace(Regex("\\s+"), " ")                     // Normalize spaces
            .trim()
    }
}
