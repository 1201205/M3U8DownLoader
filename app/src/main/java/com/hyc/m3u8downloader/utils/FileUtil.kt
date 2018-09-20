package com.hyc.m3u8downloader.utils

import android.os.Environment


val rootPath = Environment.getExternalStorageDirectory().absolutePath + "/m3u8/"
fun getAppRootPath() = rootPath
fun getUrlFilePath(url: String) = rootPath + "/" + MD5Util.crypt(url)
fun hasEnoughSpace() = Environment.getExternalStorageDirectory().freeSpace > 500 * 1024 * 1024

