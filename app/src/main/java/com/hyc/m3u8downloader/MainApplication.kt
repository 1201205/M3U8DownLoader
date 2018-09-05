package com.hyc.m3u8downloader

import android.app.Application
import kotlin.properties.Delegates

class MainApplication : Application() {
    companion object {
        var instance: MainApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}