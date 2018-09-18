package com.hyc.m3u8downloader

import com.hyc.m3u8downloader.model.MediaItem

interface IDownloadManager {
    //创建下载
    fun createNew(url: String, name: String): MediaItem
    fun pauseAll()//暂停所有
    fun startAll()//开始所有
    fun deleteAll()//删除所有
    fun getThreadCount(): Int
    fun getFileCount(): Int
    fun setThreadCount(count: Int)
    fun setFileCount(count: Int)
    fun getAllMeida(): List<MediaItem>
    fun deleteItem(item: MediaItem)

}