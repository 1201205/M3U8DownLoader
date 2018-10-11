package com.hyc.m3u8downloader.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class MediaHistory {
    @PrimaryKey
   public var filePath: String = ""
    public var time: Long = 0L
}

