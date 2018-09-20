package com.hyc.m3u8downloader.model

import android.arch.persistence.room.*
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

@Dao
interface MediaItemDao {
    //todo find how to use the one to many
    @Query("SELECT * from MediaItem")
    fun loadAllMedia(): List<MediaWithTSFiles>?

    @Query("SELECT * from MediaItem")
    fun loadAllMediaAsync(): Maybe<List<MediaWithTSFiles>>

    @Insert
    fun insertMediaAndTSFiles(item: MediaItem, list: List<TSItem>)

    @Update
    fun updateMediaAndTSFiles(item: MediaItem, list: List<TSItem>)

    @Query("SELECT * from TSItem WHERE media_id =:id ORDER BY `index` ASC")
    fun loadAllTS(id: Long): List<TSItem>

    @Query("DELETE FROM MediaItem")
    fun deleteAllMedia()

    @Query("DELETE FROM TSItem")
    fun deleteAllTSItem()

    @Insert
    fun insertTSItems(items: List<TSItem>)

    @Insert
    fun insertMedia(item: MediaItem)

    @Delete
    fun delete(item: MediaItem)

    @Delete
    fun deleteTS(file: TSItem)

    @Update
    fun updateMedia(item: MediaItem)

    @Update
    fun updateTS(item: TSItem)

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

        fun updateMediaItem(item: MediaItem) {
            Observable.create<Any> {
                MyDatabase.getInstance().getMediaItemDao().updateMedia(item)
            }.subscribeOn(Schedulers.newThread()).subscribe()
        }

        fun loadAllMediaAsync() = MyDatabase.getInstance().getMediaItemDao().loadAllMediaAsync()
        fun loadAllMedia() = MyDatabase.getInstance().getMediaItemDao().loadAllMedia()
        fun deleteItem(item: MediaItem) {
            Observable.create<Any> {
                MyDatabase.getInstance().getMediaItemDao().delete(item)
                item.list?.let {
                    for (file in it) {
                        MyDatabase.getInstance().getMediaItemDao().deleteTS(file)
                    }
                }
            }.subscribeOn(Schedulers.newThread()).subscribe()
        }

        fun updateTS(item: TSItem) = MyDatabase.getInstance().getMediaItemDao().updateTS(item)

        fun loadAllTS(item: MediaItem) = MyDatabase.getInstance().getMediaItemDao().loadAllTS(item.id!!)
        fun insertTSItems(list: List<TSItem>) = MyDatabase.getInstance().getMediaItemDao().insertTSItems(list)
        fun deleteAll() {
            Observable.create<Any> {
                MyDatabase.getInstance().getMediaItemDao().apply {
                    deleteAllMedia()
                    deleteAllTSItem()
                }

            }.subscribeOn(Schedulers.newThread()).subscribe()
        }
    }
}