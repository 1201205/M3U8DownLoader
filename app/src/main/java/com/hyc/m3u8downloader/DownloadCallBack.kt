package com.hyc.m3u8downloader

import android.arch.lifecycle.MutableLiveData
import com.hyc.m3u8downloader.model.MediaItem

interface DownloadCallBack {
    fun onDownloadSuccess(item:MutableLiveData<MediaItem>)
    fun onDownloadFailed(item:MutableLiveData<MediaItem>)
}