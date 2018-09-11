package com.hyc.m3u8downloader.model

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Maybe

@Dao
interface MediaItemDao {
    //todo find how to use the one to many
    @Query("SELECT * from MediaItem")
    fun loadAllMedia(): List<MediaWithTSFiles>

    @Insert
    fun insertMediaAndTSFiles(item: MediaItem, list: List<TSItem>)

    @Insert
    fun insertMedia(item: MediaItem)

    @Query("SELECT * from MediaItem ORDER BY id DESC LIMIT 1")
    fun loadLastItem(): Maybe<MediaItem>
}