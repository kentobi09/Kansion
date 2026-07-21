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
        val query = "$title $artist".trim()
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
}
