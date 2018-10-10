package com.hyc.m3u8downloader.utils

import android.os.Environment
import com.hyc.m3u8downloader.MainApplication


val rootPath = Environment.getExternalStorageDirectory().absolutePath + "/m3u8/"
fun getAppRootPath() = rootPath
fun getUrlFilePath(url: String) = rootPath + "/" + MD5Util.crypt(url)
fun hasEnoughSpace() = Environment.getExternalStorageDirectory().freeSpace > 500 * 1024 * 1024

/**
 * dp转px
 * @param dpValue
 * *
 * @return
 */
fun dip2px(dpValue: Float): Float {
    val scale = MainApplication.instance.resources?.displayMetrics?.density!!
    return dpValue * scale + 0.5f
}

/**
 * px转dp
 * @param pxValue
 * *
 * @return
 */
fun px2dip(pxValue: Float): Float {
    val scale = MainApplication.instance.resources?.displayMetrics?.density!!
    return pxValue / scale + 0.5f
}


fun formatTime(totalTime: Long): String {
    var hour = 0L
    var minute = 0L
    var second = totalTime / 1000L
    if (totalTime in 1..1000) {
        second = 1
    }
    if (second > 60) {
        minute = second / 60
        second %= 60
    }
    if (minute > 60) {
        hour = minute / 60
        minute %= 60
    }
    return (if (hour >= 10) hour else "0$hour").toString() + ":" + (if (minute >= 10) minute else "0$minute") + ":" + if (second >= 10) second else "0$second"
}

