package com.hyc.m3u8downloader.model

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation

class MediaWithTSFiles {
    @Embedded
    var mediaItem: MediaItem? = null
    @Relation(parentColumn = "id", entityColumn = "media_id")
    var tsFiles: List<TSItem>? = null
}