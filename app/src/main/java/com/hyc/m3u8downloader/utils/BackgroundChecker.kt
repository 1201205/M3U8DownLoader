package com.hyc.m3u8downloader.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.hyc.m3u8downloader.DownloadManager
import com.hyc.m3u8downloader.ForegroundService

object BackgroundChecker {
    private var liveCount = 0
    fun registerChecker(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityResumed(activity: Activity?) {
            }

            override fun onActivityStarted(activity: Activity?) {
                liveCount++
            }

            override fun onActivityDestroyed(activity: Activity?) {
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity?) {
                liveCount--
                if (liveCount == 0 && Config.foregroundWork && DownloadManager.getInstance().hasDownloadingFiles()) {
                    ForegroundService.Helper.startService()
                }
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            }
        })
    }
}