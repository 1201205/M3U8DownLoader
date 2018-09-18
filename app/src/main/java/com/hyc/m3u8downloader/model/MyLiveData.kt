package com.hyc.m3u8downloader.model

import android.arch.lifecycle.MutableLiveData

class MyLiveData : MutableLiveData<MediaItem>() {
    override fun postValue(value: MediaItem?) {
        super.postValue(value)
        value?.let {
            MediaItemDao.updateMediaItem(value)
        }
    }
}