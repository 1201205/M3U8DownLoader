package com.hyc.m3u8downloader

import android.arch.lifecycle.MutableLiveData
import com.hyc.m3u8downloader.model.MediaItem
import com.hyc.m3u8downloader.model.MediaWithTSFiles
import com.hyc.m3u8downloader.model.MyLiveData
import io.reactivex.Maybe
import java.util.ArrayList

interface IDownloadManager {
    //创建下载
    fun createNew(url: String, name: String): MyLiveData
    fun pauseAll()//暂停所有
    fun startAll()//开始所有
    fun deleteAll()//删除所有
    fun getThreadCount(): Int
    fun getFileCount(): Int
    fun setThreadCount(count: Int)
    fun setFileCount(count: Int)
    fun getAllMedia(): ArrayList<MutableLiveData<MediaItem>>
    fun deleteItem(item: MutableLiveData<MediaItem>)
    fun resumeItem(item: MutableLiveData<MediaItem>)

}