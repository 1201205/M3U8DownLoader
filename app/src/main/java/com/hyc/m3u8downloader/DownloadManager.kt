package com.hyc.m3u8downloader

import android.arch.lifecycle.MutableLiveData
import com.hyc.m3u8downloader.model.*
import com.hyc.m3u8downloader.utils.MD5Util
import com.hyc.m3u8downloader.utils.rootPath
import io.reactivex.Maybe

class DownloadManager:IDownloadManager {
    override fun createNew(url: String, name: String): MyLiveData {
        val item = MediaItem()
        item.url = url
        item.parentPath = rootPath + MD5Util.crypt(item.url)
        item.state = 0
        var liveData=MyLiveData()
        liveData.value=item
        FileDownloader().download(liveData, object : DownloadCallBack {
            override fun onDownloadSuccess(url: String) {
            }

            override fun onDownloadFailed(url: String) {
            }
        })
        return liveData
    }

    override fun pauseAll() {
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

    override fun getAllMedia(): Maybe<List<MediaWithTSFiles>> {
        return MediaItemDao.loadAllMediaAync()
    }

    override fun deleteItem(item: MediaItem) {
    }
    companion object {
        @Volatile
        private var INSTANCE: DownloadManager? = null

        fun getInstance(): DownloadManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?:DownloadManager().also { INSTANCE = it }
        }
    }
}