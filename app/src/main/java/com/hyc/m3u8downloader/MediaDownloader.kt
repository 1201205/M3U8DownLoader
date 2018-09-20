package com.hyc.m3u8downloader

import android.arch.lifecycle.MutableLiveData
import android.support.v4.util.SparseArrayCompat
import android.text.TextUtils
import android.util.Log
import com.hyc.m3u8downloader.model.MediaItem
import com.hyc.m3u8downloader.model.MediaItemDao
import com.hyc.m3u8downloader.model.TSItem
import com.hyc.m3u8downloader.utils.CMDUtil
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
    private var executor: ExecutorService? = null
    private var client: OkHttpClient? = null
    private var maxThreadCount: Int = DownloadManager.getInstance().getThreadCount()
    private var isDownloading = false
    private var lock: MultLock? = null
    var list: List<String>? = null
    lateinit var mItem: MutableLiveData<MediaItem>
    private var copyList: ArrayList<String> = ArrayList()
    var path: String? = null
    var file: File? = null
    var map = SparseArrayCompat<TSItem>()
    private var allTs = ArrayList<TSItem>()
    private var copyTSItems: ArrayList<TSItem> = ArrayList()
    private var checkArray = SparseArrayCompat<Long>()
    private lateinit var mediaCallback: FileDownloader.MediaMergeCallback
    private var callBack = object : M3u8Downloader.DownloadCallback {
        override fun onFileDownloadSuccess(tsFile: TSItem) {
            downloadingTS.remove(tsFile)
            mItem.value?.apply {
                this.downloadedCount++
                if (TextUtils.isEmpty(this.picPath) && tsFile.index!! > 0) {
                    synchronized(MediaDownloader@ this) {
                        try {
                            val path = this.parentPath + "/0.jpg"
                            CMDUtil.instance.exeThumb(tsFile.path!!, path, 1920, 1080, 1f)
                            val pic = File(path)
                            if (pic.exists() && pic.length() > 0) {
                                this.picPath = path
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                mItem.postValue(this)
            }

            tsFile.success = true
            MediaItemDao.updateTS(tsFile)
            lock!!.unlock()
        }

        override fun onGetContentLength(tsFile: TSItem) {
            checkArray.put(tsFile.index!!, tsFile.total)
        }

        override fun onFileDownloadFailed(tsFile: TSItem) {
            map.put(tsFile.index!!, tsFile)
            downloadingTS.remove(tsFile)
            lock!!.unlock()
        }
    }
    private var downloadingList: ArrayList<String> = ArrayList()
    private var downloadingTS: ArrayList<TSItem> = ArrayList()

//    override fun run() {
//        var count = 0
//        var fw = FileWriter(file)
//        var writer = BufferedWriter(fw)
//        try {
//            while (isDownloading) {
//                if (copyList.size == 0 && map.size() == 0 && checkArray.size() == 0) {
//                    break
//                }
//                lock!!.lock()
//                var url = getNextUrl()
//                if (!TextUtils.isEmpty(url)) {
//                    writer.write("file '$count.ts'\t\n")
//                    val request = Request.Builder().url(url).build()
//                    var call = client!!.newCall(request)
//                    executor!!.execute(M3u8Downloader(count, getFilePath(count), callBack, call))
//                    count++
//                } else if (map.size() > 0) {
//                    var key = map.keyAt(0)
//                    var value = map[key]
//                    val request = Request.Builder().url(value).build()
//                    var call = client!!.newCall(request)
//                    map.remove(key)
//                    downloadingList.add(value)
//                    executor!!.execute(M3u8Downloader(key, getFilePath(key), callBack, call))
//                } else {
//                    //检查下载
//                    while (checkArray.size() > 0) {
//                        var key = checkArray.keyAt(0)
//                        var value = checkArray[key]
//                        var file = File(getFilePath(key))
//                        var length = file.length()
//                        if (length == value) {
//                            checkArray.remove(key)
//                            Log.d("media_downloader", "check the $key file download success  it's size = $value")
//                        } else {
//                            if (downloadingList.contains(list!![key])) {
//                                //等待当前下载完毕
//                                Thread.sleep(100)
//                                continue
//                            } else {
//                                map.put(key, list!![key])
//                                Log.d("media_downloader", " the $key file download failed  it's size = $value but now $length")
//                                break
//                            }
//                        }
//                    }
//                    lock!!.unlock()
//                }
//            }
//            writer.flush()
//            fw.flush()
//            writer.close()
//            fw.close()
//            CMDUtil.instance.executeMerge(file!!.absolutePath, "$path/main.mp4")
//            Log.e("hyc-media", "success")
//            mediaCallback.onSuccess()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            mediaCallback.onFailed()
//        }
//    }


    override fun run() {
        mItem.value!!.list = MediaItemDao.loadAllTS(mItem.value!!).apply {
            allTs.addAll(this)
            copyTSItems.addAll(allTs)
        }
        mItem.value!!.fileCount = allTs.size
        var count = 0
        var fw = FileWriter(file)
        var writer = BufferedWriter(fw)
        try {
            while (isDownloading) {
                if (copyTSItems.size == 0 && map.size() == 0 && checkArray.size() == 0) {
                    break
                }
                lock!!.lock()
                var ts = getNextTS()
                if (ts != null) {
                    writer.write("file '$count.ts'\t\n")
                    if (ts.success) {
                        downloadingTS.remove(ts)
                        count++
                        lock!!.unlock()
                        continue
                    }
                    val request = Request.Builder().url(ts.url!!).build()
                    var call = client!!.newCall(request)
                    ts.path = getFilePath(count)
                    executor!!.execute(M3u8Downloader(ts, callBack, call))
                    count++
                } else if (map.size() > 0) {
                    var key = map.keyAt(0)
                    var value = map[key]
                    val request = Request.Builder().url(value.url!!).build()
                    var call = client!!.newCall(request)
                    map.remove(key)
                    downloadingTS.add(value)
                    value.path = getFilePath(key)
                    executor!!.execute(M3u8Downloader(value, callBack, call))
                } else {
                    //检查下载
                    while (checkArray.size() > 0) {
                        var key = checkArray.keyAt(0)
                        var value = checkArray[key]
                        if (value == null || value == 0L) {
                            value = allTs!![key].total
                        }
                        var file = File(getFilePath(key))
                        var length = file.length()
                        if (length >= value) {
                            checkArray.remove(key)
                            Log.d("media_downloader", "check the $key file download success  it's size = $value")
                        } else {
                            if (downloadingTS.contains(allTs!![key])) {
                                //等待当前下载完毕
                                Thread.sleep(100)
                                continue
                            } else {
                                map.put(key, allTs!![key])
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
            if (mItem.value!!.state == 2) {
                return
            }
            CMDUtil.instance.executeMerge(file!!.absolutePath, "$path/main.mp4")
            Log.e("hyc-media", "success")
            mediaCallback.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            mediaCallback.onFailed()
        }
    }


    fun stopDownload() {
        isDownloading = false
    }

    fun newInstance(): MediaDownloader {
        return MediaDownloader()
    }

    fun withClient(client: OkHttpClient): MediaDownloader {
        this.client = client
        return this
    }

    fun withExecutor(executor: ExecutorService): MediaDownloader {
        this.executor = executor
        return this
    }

    fun withMaxThreadCount(count: Int): MediaDownloader {
        maxThreadCount = count
        return this
    }

    fun download(item: MutableLiveData<MediaItem>, path: String, callback: FileDownloader.MediaMergeCallback) {
        mediaCallback = callback
        if (this.list != null) {
            throw IllegalStateException("the current downloader is downloading")
        }
        if (executor == null) {
            executor = Executors.newCachedThreadPool()
        }
        if (client == null) {
            client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(25, TimeUnit.SECONDS).build()
            Log.e("hyc--oooo", "===========")
        }
        lock = MultLock(maxThreadCount)
        copyList.addAll(item.value!!.tsUrls!!)
        isDownloading = true
        this.list = item.value!!.tsUrls!!
        mItem = item
        this.path = path
        file = File(path + "/0.txt").apply {
            this.deleteOnExit()
            createNewFile()
        }
        start()
    }

    fun download(item: MutableLiveData<MediaItem>, callback: FileDownloader.MediaMergeCallback) {
        mediaCallback = callback
        if (this.list != null) {
            throw IllegalStateException("the current downloader is downloading")
        }
        if (executor == null) {
            executor = Executors.newCachedThreadPool()
        }
        if (client == null) {
            client = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).writeTimeout(8, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()
        }
        lock = MultLock(maxThreadCount)
        isDownloading = true
//        this.list = item.value!!.tsUrls!!
        mItem = item
        this.path = item.value!!.parentPath
        file = File(path + "/0.txt").apply {
            this.deleteOnExit()
            createNewFile()
        }
        start()
    }

    private fun getNextTS(): TSItem? {
        if (copyTSItems.size == 0) {
            return null
        }
        var item = copyTSItems.removeAt(0)
        downloadingTS.add(item)
        return item
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