package com.hyc.m3u8downloader.model

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import android.util.Log
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@Dao
interface MediaItemDao {
    //todo find how to use the one to many
    @Query("SELECT * from MediaItem")
    fun loadAllMedia(): List<MediaWithTSFiles>

    @Insert
    fun insertMediaAndTSFiles(item: MediaItem, list: List<TSItem>)

    @Update
    fun updateMediaAndTSFiles(item: MediaItem, list: List<TSItem>)

    @Insert
    fun insertMedia(item: MediaItem)
    @Update
    fun updateMedia(item: MediaItem)
    @Query("SELECT * from MediaItem ORDER BY id DESC LIMIT 1")
    fun loadLastItem(): Maybe<MediaItem>

    @Query("SELECT * from MediaItem ORDER BY id DESC LIMIT 1")
    fun loadLastItemSync(): MediaItem?

    companion object {
        fun getIDAndInsert(item: MediaItem) {
            Observable.create<Any> {
                val dao = MyDatabase.getInstance().getMediaItemDao()
                val last = dao.loadLastItemSync()
                last?.let {
                    item.id = 1
                }
                if (last == null) {
                    item.id = 1
                } else {
                    item.id = last.id!! + 1
                }
                dao.insertMedia(item)
            }.subscribeOn(Schedulers.newThread()).subscribe()
        }
        fun updateMediaItem(item: MediaItem){
            Observable.create<Any> {
                MyDatabase.getInstance().getMediaItemDao().updateMedia(item)
            }.subscribeOn(Schedulers.newThread()).subscribe()
        }

    }
}