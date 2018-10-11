package com.hyc.m3u8downloader.utils

object Config {
    @JvmStatic
    var backgroundWork by Sp("background_work", true)
    @JvmStatic
    var dataWork by Sp("data_work", false)
    @JvmStatic
    var autoWork by Sp("auto_work", true)
    @JvmStatic
    var foregroundWork by Sp("foreground_work", false)

    @JvmStatic
    var maxThread by Sp("max_thread", 8)
    @JvmStatic
    var maxFile by Sp("maxFile", 4)
    @JvmStatic
    var storeMediaHistory by Sp("store_media_history", true)
}