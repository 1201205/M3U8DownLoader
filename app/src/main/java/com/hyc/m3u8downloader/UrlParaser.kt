package com.hyc.m3u8downloader

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class UrlParaser() : Thread() {
    val client: OkHttpClient = OkHttpClient()
    val queue = LinkedBlockingQueue<String>()
//    val ll = Arrays.asList<String>("application/octet-stream", "application/vnd.apple.mpegurl", "application/mpegurl", "application/x-mpegurl", "audio/mpegurl", "audio/x-mpegurl")
    override fun run() {
        while (true) {
            val url = queue.take()
            try {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().body()?.let {
                    if (it.contentType().toString().contains("video") || it.contentType().toString().contains("mpegurl") || it.contentType().toString().contains("stream")) {
                        Log.e("hyc--pass", "${url}------+${it.contentType()}")
                    }
//                    Log.e("hyc--pass","${it.contentLength()}------+${it.contentType()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }

    fun addUrl(url: String) {
        queue.put(url)
    }
}