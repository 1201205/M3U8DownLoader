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
}