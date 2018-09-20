package com.hyc.m3u8downloader.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

//@Entity(foreignKeys = [ForeignKey(entity = MediaItem::class, childColumns = arrayOf("media_id"), parentColumns = arrayOf("id"))])
@Entity
class TSItem {
    @PrimaryKey(autoGenerate = true)
    var tsId: Long? = null
    var index: Int? = null
    var path: String? = null
    var url: String? = null
    var success: Boolean = false
    var total: Long = 0

    @ColumnInfo(name = "media_id")
    var mediaId: Long? = null

    constructor(index: Int?, url: String?, mediaId: Long?) {
        this.index = index
        this.url = url
        this.mediaId = mediaId
        this.path = path
    }


    override fun toString(): String {
        return "TSItem(tsId=$tsId, index=$index, path=$path, url=$url, mediaId=$mediaId)"
    }
}