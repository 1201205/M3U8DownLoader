package com.hyc.m3u8downloader.model

import android.arch.persistence.room.TypeConverter
import com.hyc.m3u8downloader.DownloadState
object StateConverter{
    @TypeConverter
    @JvmStatic
    fun toState(index: Int) = DownloadState.values()[index]

    @TypeConverter
    @JvmStatic
    fun toInteger(state: DownloadState) = state.ordinal
}
