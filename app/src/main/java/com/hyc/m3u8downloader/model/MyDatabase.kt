package com.hyc.m3u8downloader.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import com.hyc.m3u8downloader.MainApplication

@Database(entities = [MediaItem::class, TSItem::class], version = 1)
abstract class MyDatabase : RoomDatabase() {
    abstract fun getMediaItemDao():MediaItemDao
    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getInstance(): MyDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase().also { INSTANCE = it }
        }

        private fun buildDatabase() = Room.databaseBuilder(MainApplication.instance.applicationContext, MyDatabase::class.java, "data.db").build()
    }

}