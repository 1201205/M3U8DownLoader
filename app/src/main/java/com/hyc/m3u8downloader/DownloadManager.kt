package com.hyc.m3u8downloader

import android.arch.lifecycle.MutableLiveData
import android.text.TextUtils
import com.hyc.m3u8downloader.model.*
import com.hyc.m3u8downloader.utils.MD5Util
import com.hyc.m3u8downloader.utils.Sp
import com.hyc.m3u8downloader.utils.rootPath
import okhttp3.OkHttpClient
import java.io.File
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class DownloadManager : IDownloadManager {
    private val downloadingItems: ArrayList<FileDownloader> = ArrayList()
    private val waitingItems: ArrayList<MutableLiveData<MediaItem>> = ArrayList()
    private lateinit var allItems: ArrayList<MutableLiveData<MediaItem>>
    private var maxDownloadingCount by Sp("max_downloading_count", 3)
    private var maxThreadCount by Sp("max_thread_count", 6)
    private val mClient = OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build()
    private val mLockMap = HashMap<MutableLiveData<MediaItem>, MultLock>()//因为不想添加暂停中状态 todo 成功回调中去除lock
    private val mExecutor = ThreadPoolExecutor(0, Integer.MAX_VALUE,
            20L, TimeUnit.SECONDS,
            SynchronousQueue(), DefaultThreadFactory())

    override fun createNew(url: String, name: String): MyLiveData {
        val item = MediaItem()
        item.url = url
        item.parentPath = rootPath + MD5Util.crypt(item.url)
        item.state = 0
        val liveData = MyLiveData()
        liveData.value = item
        allItems.add(0, liveData)
        if (checkCreateDownloader()) {
            createDownloader(liveData)
        } else {
            waitingItems.add(liveData)
        }
        MediaItemDao.getIDAndInsert(item)
        return liveData
    }


    override fun pauseAll() {
        for (item in downloadingItems) {
            item.stopDownload()
            item.mItem!!.apply {
                this.value!!.state = 2
                this.postValue(this.value)
            }
            downloadingItems.remove(item)
            waitingItems.add(item.mItem!!)
        }
    }

    override fun startAll() {
        for (item in allItems) {
            if (checkCreateDownloader()) {
                createDownloader(item)
            } else {
                item.value!!.state = 0
                item.postValue(item.value)
                waitingItems.add(item)
            }
        }
    }

    override fun deleteAll() {
        for (downloader in downloadingItems) {
            downloader.stopDownload()
            downloadingItems.remove(downloader)
        }
        mLockMap.clear()
        waitingItems.clear()
        for (item in allItems) {
            deleteCacheFiles(item.value!!.parentPath)
        }
        MediaItemDao.deleteAll()
    }

    override fun getThreadCount(): Int {
        return maxThreadCount
    }

    override fun getFileCount(): Int {
        return maxDownloadingCount
    }

    override fun setThreadCount(count: Int) {
        if (count < 1 || count > 8) {
            return
        }
        maxThreadCount = count

    }

    override fun setFileCount(count: Int) {
        if (count < 1 || count > 4) {
            return
        }
        maxDownloadingCount = count
    }

    override fun getAllMedia(): ArrayList<MutableLiveData<MediaItem>> {
        var list = MediaItemDao.loadAllMedia()
        var target = ArrayList<MutableLiveData<MediaItem>>()
        if (list != null && !list.isEmpty()) {
            for (item in list) {
                var media = item.mediaItem
                media!!.list = item.tsFiles
                if (media.state != 3) {
                    media.state = 2
                }
                var data = MyLiveData()
                data.postValue(media)
                target.add(data)
            }
        }
        allItems = target
        return target
    }

    override fun deleteItem(item: MutableLiveData<MediaItem>) {
        for (downloader in downloadingItems) {
            if (downloader.isThisDownloading(item)) {
                downloader.stopDownload()
                downloadingItems.remove(downloader)
                break
            }
        }
        mLockMap.remove(item)
        allItems.remove(item)
        deleteCacheFiles(item.value!!.parentPath)
        MediaItemDao.deleteItem(item.value!!)
        downloadNext()
    }

    override fun resumeItem(item: MutableLiveData<MediaItem>) {
        createDownloader(item)
    }

    private fun deleteCacheFiles(parentPath: String?) {
        if (TextUtils.isEmpty(parentPath)) {
            return
        }
        val file = File(parentPath)
        file.deleteOnExit()
    }

    override fun pauseItem(item: MutableLiveData<MediaItem>) {
        for (downloader in downloadingItems) {
            if (downloader.isThisDownloading(item)) {
                downloader.stopDownload()
                item.value!!.state = 2
                item.postValue(item.value)
                downloadingItems.remove(downloader)
                break
            }
        }
        downloadNext()
    }

    private fun checkCreateDownloader() = downloadingItems.size < getFileCount()

    private fun downloadNext() {
        if (waitingItems.isEmpty()) {
            return
        }
        createDownloader(waitingItems.removeAt(0))
    }

    private fun createDownloader(item: MutableLiveData<MediaItem>) {
        var lock: MultLock? = null
        if (mLockMap.containsKey(item)) {
            lock = mLockMap[item]
        }
        if (lock == null) {
            lock = MultLock(maxThreadCount)
            mLockMap.put(item, lock)
        }
        val downloader = FileDownloader(mClient, mExecutor, lock)
        downloadingItems.add(downloader)
        downloader.download(item, object : DownloadCallBack {
            override fun onDownloadSuccess(item: MutableLiveData<MediaItem>) {
                downloadingItems.remove(downloader)
                mLockMap.remove(item)
                downloadNext()
            }

            override fun onDownloadFailed(item: MutableLiveData<MediaItem>) {
                downloadingItems.remove(downloader)
                mLockMap.remove(item)
                downloadNext()
            }
        })
    }

    fun hasDownloadingFiles() = downloadingItems.size > 0

    companion object {
        @Volatile
        private var INSTANCE: DownloadManager? = null

        fun getInstance(): DownloadManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: DownloadManager().also { INSTANCE = it }
        }
    }


    private class DefaultThreadFactory internal constructor() : ThreadFactory {
        private val group: ThreadGroup
        private val threadNumber = AtomicInteger(1)
        private val namePrefix: String

        init {
            val s = System.getSecurityManager()
            group = if (s != null)
                s.threadGroup
            else
                Thread.currentThread().threadGroup
            namePrefix = "m3u8-" +
                    poolNumber.getAndIncrement() +
                    "-t-"
        }

        override fun newThread(r: Runnable): Thread {
            val t = Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0)
            if (t.isDaemon)
                t.isDaemon = false
            if (t.priority != Thread.NORM_PRIORITY)
                t.priority = Thread.NORM_PRIORITY
            return t
        }

        companion object {
            private val poolNumber = AtomicInteger(1)
        }
    }
}