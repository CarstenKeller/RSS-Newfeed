package com.carstenkeller.rssnewfeed.data.network

import android.content.Context
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.OkHttpClient

/** Shared, disk-cached OkHttp client so article images already seen once are available offline. */
object HttpClients {
    private const val CACHE_SIZE_BYTES = 25L * 1024 * 1024

    @Volatile private var instance: OkHttpClient? = null

    fun imageClient(context: Context): OkHttpClient =
        instance ?: synchronized(this) {
            instance ?: OkHttpClient.Builder()
                .cache(Cache(File(context.applicationContext.cacheDir, "http_image_cache"), CACHE_SIZE_BYTES))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
                .also { instance = it }
        }
}
