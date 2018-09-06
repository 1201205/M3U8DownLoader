package com.hyc.m3u8downloader

import android.app.Application
import android.util.Log
import com.hyc.m3u8downloader.utils.CMDUtil
import kotlin.properties.Delegates

class MainApplication : Application() {
    companion object {
        var instance: MainApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("cmd", CMDUtil.instance.canUseCMD().toString())
    }
}