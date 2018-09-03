package com.hyc.m3u8downloader

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.ArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.locks.AbstractQueuedSynchronizer

class MediaDownloader : Thread() {
    var executor: ExecutorService? = null
    var client: OkHttpClient? = null
    var maxThreadCount: Int = 5
    var isDownloading = false
    private var lock: MultLock? = null
    var list: List<String>? = null
    var copyList: ArrayList<String> = ArrayList()
    var path: String? = null
    private var downloadingList: ArrayList<String> = ArrayList()
    override fun run() {
        var count = 0
        while (isDownloading) {
            if (copyList.size == 0) {
                return
            }
            lock!!.lock()
            var url = getNextUrl()
            val request = Request.Builder().url(url).build()
            var call = client!!.newCall(request)
            executor!!.execute(M3u8Downloader(getFilePath(count), lock!!, call))
            count++
        }
    }

    fun newInstance(): MediaDownloader {
        return MediaDownloader()
    }

    fun withClient(client: OkHttpClient): MediaDownloader {
        this.client = client
        return this
    }

    fun withExecutor(executor: ThreadPoolExecutor): MediaDownloader {
        this.executor = executor
        return this
    }

    fun withMaxThreadCount(count: Int): MediaDownloader {
        maxThreadCount = count
        return this
    }

    fun download(list: List<String>) {
        if (this.list != null) {
            throw IllegalStateException("the current downloader is downloading")
        }
        if (executor == null) {
            executor = Executors.newCachedThreadPool()
        }
        if (client == null) {
            client = OkHttpClient()
        }
        lock = MultLock(maxThreadCount)
        copyList.addAll(list)
        isDownloading = true
        this.list = list
        start()
    }
    private fun getNextUrl(): String {
        var url = copyList.removeAt(0)
        downloadingList.add(url)
        return url
    }

    private fun getFilePath(count: Int): String {
        return "/sdcard/1/${count}.ts"//for test
    }
}