package com.hyc.m3u8downloader.utils

import android.content.ContentResolver
import android.provider.Settings
import com.hyc.m3u8downloader.MainApplication

class BrightnessUtil() {
    var resolver: ContentResolver = MainApplication.instance.contentResolver

    fun changeBirghtness(changed: Int) {
        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, checkBrightness(getBrightness() + changed))
    }

    fun closeAutoBrightness() {
        try {
            if (Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun openAutoBrightness() {
        try {
            if (Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE) != Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBrightness() = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 255)
    private fun checkBrightness(brightness: Int): Int {
        if (brightness < 0) {
            return 0
        } else if (brightness > 255) {
            return 255
        }
        return brightness
    }


    companion object {
        @Volatile
        private var INSTANCE: BrightnessUtil? = null

        fun getInstance(): BrightnessUtil = INSTANCE ?: synchronized(this) {
            INSTANCE ?: BrightnessUtil().also { INSTANCE = it }
        }
    }
}