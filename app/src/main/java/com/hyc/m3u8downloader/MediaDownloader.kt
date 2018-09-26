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
    private lateinit var mDownloadCallBack: DownloadCallBack
    private var callBack = object : M3u8Downloader.DownloadCallback {
        override fun onFileDownloadSuccess(tsFile: TSItem) {
            downloadingTS.remove(tsFile)
            mItem.value?.apply {
                this.downloadedCount++
                if (downloadedCount > fileCount!!) {
                    downloadedCount = list!!.count { it.success }
                }
                synchronized(MediaDownloader@ this) {
                    if (TextUtils.isEmpty(this.picPath)) {
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
            Log.e("hyc-thread", "${lock!!.getLiveCount()}-----+${lock.toString()}")
        }

        override fun onGetContentLength(tsFile: TSItem) {
            if (tsFile.success && File(tsFile.path).exists() && File(tsFile.path).length() >= tsFile.total) {
                Log.e("media_downloader", "some error while downloading")
                return
            }
            checkArray.put(tsFile.index!!, tsFile.total)
        }

        override fun onFileDownloadFailed(tsFile: TSItem) {
            map.put(tsFile.index!!, tsFile)
            downloadingTS.remove(tsFile)
            lock!!.unlock()
            Log.e("hyc-thread", "${lock!!.getLiveCount()}-----+${lock.toString()}")
        }
    }
    private var downloadingList: ArrayList<String> = ArrayList()
    private var downloadingTS: ArrayList<TSItem> = ArrayList()

    override fun run() {
        mItem.value!!.list = MediaItemDao.loadAllTS(mItem.value!!).apply {
            allTs.addAll(this)
            copyTSItems.addAll(allTs)
        }
        mItem.value!!.downloadedCount = allTs.count { it.success }
        mItem.value!!.fileCount = allTs.size
        var count = 0
        var fw = FileWriter(file)
        var writer = BufferedWriter(fw)
        try {
            while (isDownloading && !isInterrupted) {
                if (copyTSItems.size == 0 && map.size() == 0 && checkArray.size() == 0) {
                    break
                }
                lock!!.lock()
                var ts = getNextTS()
                if (ts != null) {
                    if (ts.success) {
                        downloadingTS.remove(ts)
                        count++
                        checkArray.put(ts.index!!, ts.total)
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
                    map.remove(key)
                    if (downloadingTS.contains(value)) {
                        lock!!.unlock()
                        continue
                    }
                    val request = Request.Builder().url(value.url!!).build()
                    var call = client!!.newCall(request)
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
//                        if (value == 0L) {
//                            map.put(key, allTs!![key])
//                            Log.d("media_downloader", " the $key file download failed  it's size = $value ")
//                            break
//                        }
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
            //无法避免一些ts文件总长度为0的情况
            for (item in allTs) {
                if (item.total > 0) {
                    writer.write("file '${item.index}.ts'\t\n")
                }
            }
            writer.flush()
            fw.flush()
            writer.close()
            fw.close()
            if (mItem.value!!.state == DownloadState.STOPPED || isInterrupted) {
                return
            }

            var fileName = mItem.value!!.name
            if (TextUtils.isEmpty(fileName)) {
                fileName = "main"
            }
            val mp4Path = "$path/$fileName.mp4"
            mItem.value!!.state = DownloadState.MERGING
            mItem.postValue(mItem.value)
            CMDUtil.instance.executeMerge(file!!.absolutePath, mp4Path)
            mItem.value!!.state = DownloadState.SUCCESS
            mItem.value!!.mp4Path = mp4Path
            mItem.postValue(mItem.value)
            Log.e("hyc-media", "success")
            deleteFile(File(mItem.value!!.parentPath))
            MediaItemDao.deleteTSByItem(mItem.value!!)
            mDownloadCallBack.onDownloadSuccess(mItem)
        } catch (e: Exception) {
            e.printStackTrace()
            mDownloadCallBack.onDownloadFailed(mItem)
        }
    }

    private fun deleteFile(file: File) {
        //目前先订为mp4
        if (file.isFile && !(file.absolutePath.endsWith(".mp4") || file.absolutePath.endsWith(".jpg"))) {
            file.delete()
        } else if (file.isDirectory) {
            file.listFiles().forEach {
                deleteFile(it)
            }
        }
    }

    fun stopDownload() {
        isDownloading = false
        interrupt()
    }

    fun withClient(client: OkHttpClient): MediaDownloader {
        this.client = client
        return this
    }

    fun withExecutor(executor: ExecutorService): MediaDownloader {
        this.executor = executor
        return this
    }

    fun withLock(lock: MultLock): MediaDownloader {
        this.lock = lock
        return this
    }


    fun download(item: MutableLiveData<MediaItem>, callback: DownloadCallBack) {
        mDownloadCallBack = callback
        if (this.list != null) {
            throw IllegalStateException("the current downloader is downloading")
        }
        if (executor == null) {
            executor = Executors.newCachedThreadPool()
        }
        if (client == null) {
            client = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).writeTimeout(8, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()
        }
        if (lock == null) {
            lock = MultLock(maxThreadCount)
        }
        isDownloading = true
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
        return "$path/$count.ts"
    }
}