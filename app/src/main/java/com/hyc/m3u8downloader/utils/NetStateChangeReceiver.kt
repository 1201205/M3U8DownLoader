package com.hyc.m3u8downloader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.content.IntentFilter
import android.net.NetworkInfo
import android.util.Log
import android.widget.Toast
import com.hyc.m3u8downloader.DownloadManager
import com.hyc.m3u8downloader.MainApplication


class NetStateChangeReceiver : BroadcastReceiver() {
    private var currentState = 0
    private val connectivityManager = MainApplication.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        val info = connectivityManager.activeNetworkInfo
        currentState = getConnectState(info)
        Log.e("hyc-net", "current net state is $currentState")
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        MainApplication.instance.registerReceiver(this, intentFilter)
    }

    fun getNetState(): Int = currentState
    private fun getConnectState(info: NetworkInfo?): Int {
        if (info == null) {
            return STATE_NO_CONNECT
        }
        if (!info.isAvailable) {
            return STATE_NO_CONNECT
        }
        var type = info.type
        when (type) {
            ConnectivityManager.TYPE_WIFI -> return STATE_CONNECT_WIFI
        }
        return STATE_CONNECT_OTHER
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) {
            return
        }
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val info = connectivityManager.activeNetworkInfo
            currentState = getConnectState(info)
            Log.e("hyc-net", "state changed current net state is $currentState")

            when (currentState) {
                STATE_NO_CONNECT -> DownloadManager.getInstance().pauseAll()
                STATE_CONNECT_WIFI -> if (Config.autoWork) {
                    DownloadManager.getInstance().startAll()
                }
                STATE_CONNECT_OTHER -> if (!Config.dataWork) {
                    if (DownloadManager.getInstance().hasDownloadingFiles()) {
                        DownloadManager.getInstance().pauseAll()
                        Toast.makeText(MainApplication.instance, "当前使用非WIFI连接，已自动暂停所有下载", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }


    companion object {
        @Volatile
        private var INSTANCE: NetStateChangeReceiver? = null
        const val STATE_NO_CONNECT = 0
        const val STATE_CONNECT_WIFI = 1
        const val STATE_CONNECT_OTHER = 2
        fun getInstance(): NetStateChangeReceiver = INSTANCE ?: synchronized(this) {
            INSTANCE ?: NetStateChangeReceiver().also { INSTANCE = it }
        }
    }
}