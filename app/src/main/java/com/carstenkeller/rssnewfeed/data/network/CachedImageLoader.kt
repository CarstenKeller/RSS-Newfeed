package com.carstenkeller.rssnewfeed.data.network

import android.content.Context
import android.webkit.WebResourceResponse
import java.io.IOException
import okhttp3.CacheControl
import okhttp3.Request
import okhttp3.Response

private val IMAGE_EXTENSION_REGEX = Regex(".*\\.(jpg|jpeg|png|gif|webp|bmp|svg)(\\?.*)?$", RegexOption.IGNORE_CASE)

/**
 * Loads an article-detail WebView's own `<img>` requests through the shared disk-cached
 * OkHttp client, so images already fetched once remain visible when offline. The WebView's
 * default network stack has no cache shared with the rest of the app, which is why this is
 * needed instead of just relying on Coil (used for the list thumbnails) or the WebView itself.
 */
object CachedImageLoader {

    fun load(context: Context, url: String): WebResourceResponse? {
        if (!IMAGE_EXTENSION_REGEX.matches(url)) return null
        val client = HttpClients.imageClient(context)
        val response = fetchWithCacheFallback(client, url) ?: return null
        return try {
            if (!response.isSuccessful) {
                response.close()
                return null
            }
            val mimeType = response.header("Content-Type")
                ?.substringBefore(";")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: "image/*"
            WebResourceResponse(mimeType, null, response.body?.byteStream())
        } catch (e: Exception) {
            response.close()
            null
        }
    }

    private fun fetchWithCacheFallback(client: okhttp3.OkHttpClient, url: String): Response? {
        return try {
            client.newCall(Request.Builder().url(url).build()).execute()
        } catch (e: IOException) {
            // Offline: fall back to whatever OkHttp already has cached for this URL, if anything.
            try {
                client.newCall(Request.Builder().url(url).cacheControl(CacheControl.FORCE_CACHE).build()).execute()
            } catch (e2: Exception) {
                null
            }
        }
    }
}
