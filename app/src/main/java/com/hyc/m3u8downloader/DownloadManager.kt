package com.hyc.m3u8downloader

import android.arch.lifecycle.MutableLiveData
import android.text.TextUtils
import com.hyc.m3u8downloader.model.*
import com.hyc.m3u8downloader.utils.MD5Util
import com.hyc.m3u8downloader.utils.rootPath
import java.io.File
import java.util.ArrayList

class DownloadManager : IDownloadManager {
    private val downloadingItems: ArrayList<FileDownloader> = ArrayList()
    private lateinit var allItems: ArrayList<MutableLiveData<MediaItem>>
    override fun createNew(url: String, name: String): MyLiveData {
        val item = MediaItem()
        item.url = url
        item.parentPath = rootPath + MD5Util.crypt(item.url)
        item.state = 0
        val liveData = MyLiveData()
        liveData.value = item
        val downloader = FileDownloader()
        downloadingItems.add(downloader)
        allItems.add(0,liveData)
        downloader.download(liveData, object : DownloadCallBack {
            override fun onDownloadSuccess(url: String) {
                downloadingItems.remove(downloader)
                downloadNext()
            }

            override fun onDownloadFailed(url: String) {
                downloadNext()
            }
        })
        return liveData
    }

    private fun downloadNext() {

    }

    override fun pauseAll() {
        for (item in downloadingItems) {
            item.stopDownload()
        }
    }

    override fun startAll() {
    }

    override fun deleteAll() {
    }

    override fun getThreadCount(): Int {
        return 0
    }

    override fun getFileCount(): Int {
        return 0
    }

    override fun setThreadCount(count: Int) {
    }

    override fun setFileCount(count: Int) {
    }

    override fun getAllMedia(): ArrayList<MutableLiveData<MediaItem>> {
        var list = MediaItemDao.loadAllMedia()
        var target = ArrayList<MutableLiveData<MediaItem>>()
        if (list != null && !list.isEmpty()) {
            for (item in list) {
                var media = item.mediaItem
                media!!.list = item.tsFiles
                var data = MyLiveData()
                data.postValue(media)
                target.add(data)
            }
        }
        allItems = target
        return target
    }

    override fun deleteItem(item: MutableLiveData<MediaItem>) {
//        when (item.value!!.state) {
//            0 -> {
//
//            }
//        }
        for (downloader in downloadingItems) {
            if (downloader.isThisDownloading(item)) {
                downloader.stopDownload()
                break
            }
        }
        allItems.remove(item)
        deleteCacheFiles(item.value!!.parentPath)
        MediaItemDao.deleteItem(item.value!!)
    }

    private fun deleteCacheFiles(parentPath: String?) {
        if (TextUtils.isEmpty(parentPath)) {
            return
        }
        val file = File(parentPath)
        file.deleteOnExit()
    }

    override fun resumeItem(item: MutableLiveData<MediaItem>) {
        val downloader = FileDownloader()
        downloadingItems.add(downloader)
        downloader.download(item, object : DownloadCallBack {
            override fun onDownloadSuccess(url: String) {
                downloadingItems.remove(downloader)
                downloadNext()
            }

            override fun onDownloadFailed(url: String) {
                downloadingItems.remove(downloader)
                downloadNext()
            }
        })
    }

    fun resumeItem(pos:Int){
//        if (allItems.get(pos).value!!.state == 1) {
//            for (downloader in downloadingItems) {
//                if (downloader.isThisDownloading(allItems.get(pos))) {
//                    downloader.stopDownload()
//                    allItems.get(pos).value!!.state = 2
//                    downloadingItems.remove(downloader)
//                    break
//                }
//            }
//        } else {
            resumeItem(allItems.get(pos))
//        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DownloadManager? = null

        fun getInstance(): DownloadManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: DownloadManager().also { INSTANCE = it }
        }
    }
}