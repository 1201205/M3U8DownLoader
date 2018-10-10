package com.hyc.m3u8downloader.utils

import android.content.ContentResolver
import android.provider.Settings
import com.hyc.m3u8downloader.MainApplication

class BrightnessController {
    private  val resolver: ContentResolver = MainApplication.instance.contentResolver
    private var needChangeSetting=false
    fun changeBrightness(changed: Int) {
        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, changed)
    }

    fun closeAutoBrightness() {
        needChangeSetting = try {
            if (Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    fun openAutoBrightness() {
        try {
            if (needChangeSetting&&Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE) != Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBrightness() = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 255)
    private fun checkBrightness(brightness: Int): Int {
        if (brightness < 0) {
            return 0
        } else if (brightness > 255) {
            return 255
        }
        return brightness
    }
}