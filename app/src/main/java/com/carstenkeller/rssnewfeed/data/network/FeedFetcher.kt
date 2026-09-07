package com.carstenkeller.rssnewfeed.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class FeedFetcher(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
) {
    suspend fun fetchAndParse(url: String): ParsedFeed = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("HTTP ${response.code} beim Laden von $url")
            }
            val body = response.body ?: error("Leere Antwort von $url")
            body.byteStream().use { stream -> RssParser.parse(stream) }
        }
    }
}
