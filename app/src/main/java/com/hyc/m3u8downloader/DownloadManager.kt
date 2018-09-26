package com.hyc.m3u8downloader

import android.arch.lifecycle.MutableLiveData
import android.text.TextUtils
import com.hyc.m3u8downloader.DownloadState.*
import com.hyc.m3u8downloader.model.*
import com.hyc.m3u8downloader.utils.MD5Util
import com.hyc.m3u8downloader.utils.Sp
import com.hyc.m3u8downloader.utils.rootPath
import okhttp3.OkHttpClient
import java.io.File
import java.util.ArrayList
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
    private val mLockMap = HashMap<MutableLiveData<MediaItem>, MultLock>()//因为不想添加暂停中状态
    private val mExecutor = ThreadPoolExecutor(0, Integer.MAX_VALUE,
            20L, TimeUnit.SECONDS,
            SynchronousQueue(), DefaultThreadFactory())
    private var inited = false
    override fun createNew(url: String, name: String): Boolean {
        val historyItem = allItems.filter {
            it.value!!.url.equals(url)
        }
        if (!historyItem.isEmpty()) {
            return false
        }

        val item = MediaItem()
        item.url = url
        if (TextUtils.isEmpty(name)) {
            item.parentPath = rootPath + MD5Util.crypt(item.url)
        } else {
            item.parentPath = rootPath + name
        }
        item.name = name
        item.state = WAITING
        item.picPath = ""
        val liveData = MyLiveData()
        liveData.value = item
        allItems.add(0, liveData)
        if (checkCreateDownloader()) {
            createDownloader(liveData)
        } else {
            waitingItems.add(liveData)
        }
        MediaItemDao.getIDAndInsert(item)
        return true
    }


    override fun pauseAll() {
        for (item in downloadingItems) {
            item.stopDownload()
            item.mItem!!.apply {
                this.value!!.state = STOPPED
                this.postValue(this.value)
            }
            waitingItems.add(item.mItem!!)
        }
        downloadingItems.clear()
    }

    override fun startAll() {
        if (!inited) {
            return
        }
        for (item in allItems) {
            resumeItem(item)
        }
    }

    override fun deleteAll() {
        for (downloader in downloadingItems) {
            downloader.stopDownload()
        }
        downloadingItems.clear()
        mLockMap.clear()
        waitingItems.clear()
        for (item in allItems) {
            deleteCacheFiles(item.value!!.parentPath)
        }
        allItems.clear()
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
        val list = MediaItemDao.loadAllMedia()
        val target = ArrayList<MutableLiveData<MediaItem>>()
        if (!(list == null || list.isEmpty())) {
            for (item in list) {
                val media = item.mediaItem
                media!!.list = item.tsFiles
                if (media.state != SUCCESS) {
                    media.state = STOPPED
                }
                val data = MyLiveData()
                data.postValue(media)
                target.add(data)
            }
        }
        allItems = target
        inited = true
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
        if (item.value == null) {
            return
        }
        if (item.value!!.state in arrayOf(FAiLED, STOPPED, WAITING)) {
            if (checkCreateDownloader()) {
                createDownloader(item)
            } else {
                item.value!!.state = WAITING
                item.postValue(item.value)
                waitingItems.add(item)
            }
        }
    }

    private fun deleteCacheFiles(parentPath: String?) {
        if (TextUtils.isEmpty(parentPath)) {
            return
        }
        val file = File(parentPath)
        if (!file.exists()) {
            return
        }
        for (fileItem in file.listFiles()) {
            if (fileItem.isDirectory) {
                deleteCacheFiles(fileItem.absolutePath)
            } else {
                fileItem.delete()
            }
        }
        file.delete()
    }

    override fun pauseItem(item: MutableLiveData<MediaItem>) {
        for (downloader in downloadingItems) {
            if (downloader.isThisDownloading(item)) {
                downloader.stopDownload()
                item.value!!.state = STOPPED
                item.postValue(item.value)
                downloadingItems.remove(downloader)
                break
            }
        }
        downloadNext()
    }

    override fun reDownloadItem(item: MutableLiveData<MediaItem>) {
        deleteItem(item)
        allItems.remove(item)
        createNew(item.value!!.url!!, item.value!!.name!!)
    }

    override fun hasItems(): Boolean = !allItems.isEmpty()
    private fun checkCreateDownloader() = downloadingItems.size < getFileCount()

    private fun downloadNext() {
        if (waitingItems.isEmpty()) {
            return
        }
        createDownloader(waitingItems.removeAt(0))
    }

    private fun createDownloader(item: MutableLiveData<MediaItem>) {
        for (downItem in downloadingItems) {
            if (downItem.isThisDownloading(item)) {
                return
            }
        }
        var lock: MultLock? = null
        if (mLockMap.containsKey(item)) {
            lock = mLockMap[item]
        }
        if (lock == null) {
            lock = MultLock(maxThreadCount)
            mLockMap[item] = lock
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