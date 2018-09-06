package com.hyc.m3u8downloader

import android.support.v4.util.SparseArrayCompat
import android.text.TextUtils
import com.hyc.m3u8downloader.utils.CMDUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
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
    var file: File? = null
    var map = SparseArrayCompat<String>()
    var callBack = object : M3u8Downloader.DownloadCallback {
        override fun onFileDownloadSuccess() {
            lock!!.unlock()
        }

        override fun onFileDownloadFailed(url: String, index: Int) {
            map.put(index, url)
            lock!!.unlock()
        }

    }
    private var downloadingList: ArrayList<String> = ArrayList()
    override fun run() {
        var count = 0
        var fw = FileWriter(file)
        var writer = BufferedWriter(fw)
        try {
            while (isDownloading) {
                if (copyList.size == 0 && map.size() == 0) {
                    break
                }
                lock!!.lock()
                var url = getNextUrl()
                if (!TextUtils.isEmpty(url)) {
                    writer.write("file '$count.ts'\t\n")
                    val request = Request.Builder().url(url).build()
                    var call = client!!.newCall(request)
                    executor!!.execute(M3u8Downloader(count, getFilePath(count), callBack, call))
                    count++
                } else {
                    var key = map.keyAt(0)
                    var value = map[key]
                    val request = Request.Builder().url(value).build()
                    var call = client!!.newCall(request)
                    map.remove(key)
                    executor!!.execute(M3u8Downloader(key, getFilePath(key), callBack, call))
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        writer.flush()
        fw.flush()
        writer.close()
        fw.close()
        CMDUtil.instance.executeMerge(file!!.absolutePath, path + "/main.mp4")

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

    fun download(list: List<String>, path: String) {
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
        this.path = path
        file = File(path + "/0.txt").apply {
            this.deleteOnExit()
            createNewFile()
        }
        start()
    }

    private fun getNextUrl(): String? {
        if (copyList.size == 0) {
            return null
        }
        var url = copyList.removeAt(0)
        downloadingList.add(url)
        return url
    }

    private fun getFilePath(count: Int): String {
        return path + "/" + count + ".ts"
    }
}