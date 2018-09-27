package com.hyc.m3u8downloader

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.hyc.m3u8downloader.utils.BackgroundChecker
import com.hyc.m3u8downloader.utils.CMDUtil
import com.hyc.m3u8downloader.utils.NetStateChangeReceiver
import com.tencent.bugly.Bugly
import kotlin.properties.Delegates

class MainApplication : Application() {

    companion object {
        var instance: MainApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Bugly.init(this, "11dfe72ef9", false)
        NetStateChangeReceiver.getInstance()
        Log.d("cmd", CMDUtil.instance.canUseCMD().toString())
        BackgroundChecker.registerChecker(this)
    }
}