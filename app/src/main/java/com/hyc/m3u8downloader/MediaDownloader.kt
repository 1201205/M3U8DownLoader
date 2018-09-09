package com.hyc.m3u8downloader

import android.support.v4.util.SparseArrayCompat
import android.text.TextUtils
import android.util.Log
import com.hyc.m3u8downloader.utils.CMDUtil
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.util.ArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MediaDownloader : Thread() {
    /**
     * 问题：如何确保下载的东西已经完全下载完了
     * 目前的处理：
     * 1.使用list保存待处理的下载地址-->先下载这些
     * 2.使用sparseArray下载失败的地址-->优先级次之
     * 未做的处理：
     * 1.保存每个下载链接的contentLength ,最后进行文件校验
     * 2.这个步骤放在上面两步之后
     *
     */
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
    var checkArray = SparseArrayCompat<Long>()
    lateinit var mediaCallback: FileDownloader.MediaMergeCallback
    private var callBack = object : M3u8Downloader.DownloadCallback {
        override fun onGetContentLength(index: Int, length: Long) {
            checkArray.put(index, length)
        }

        override fun onFileDownloadSuccess(url: String) {
            lock!!.unlock()
            downloadingList.remove(url)
        }

        override fun onFileDownloadFailed(url: String, index: Int) {
            map.put(index, url)
            lock!!.unlock()
            downloadingList.remove(url)
        }

    }
    private var downloadingList: ArrayList<String> = ArrayList()
    override fun run() {
        var count = 0
        var fw = FileWriter(file)
        var writer = BufferedWriter(fw)
        try {
            while (isDownloading) {
                if (copyList.size == 0 && map.size() == 0 && checkArray.size() == 0) {
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
                } else if (map.size() > 0) {
                    var key = map.keyAt(0)
                    var value = map[key]
                    val request = Request.Builder().url(value).build()
                    var call = client!!.newCall(request)
                    map.remove(key)
                    downloadingList.add(value)
                    executor!!.execute(M3u8Downloader(key, getFilePath(key), callBack, call))
                } else {
                    //检查下载
                    while (checkArray.size() > 0) {
                        var key = checkArray.keyAt(0)
                        var value = checkArray[key]
                        var file = File(getFilePath(key))
                        var length = file.length()
                        if (length == value) {
                            checkArray.remove(key)
                            Log.d("media_downloader", "check the $key file download success  it's size = $value")
                        } else {
                            if (downloadingList.contains(list!![key])) {
                                //等待当前下载完毕
                                Thread.sleep(100)
                                continue
                            } else {
                                map.put(key, list!![key])
                                Log.d("media_downloader", " the $key file download failed  it's size = $value but now $length")
                                break
                            }

                        }
                    }
                    lock!!.unlock()
                }

            }
            writer.flush()
            fw.flush()
            writer.close()
            fw.close()
            CMDUtil.instance.executeMerge(file!!.absolutePath, "$path/main.mp4")
            Log.e("hyc-media","success")
            mediaCallback.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            mediaCallback.onFailed()
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

    fun download(list: List<String>, path: String, callback: FileDownloader.MediaMergeCallback) {
        mediaCallback = callback
        if (this.list != null) {
            throw IllegalStateException("the current downloader is downloading")
        }
        if (executor == null) {
            executor = Executors.newCachedThreadPool()
        }
        if (client == null) {
            client = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).writeTimeout(8, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS).build()
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