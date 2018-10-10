package com.hyc.m3u8downloader.utils

import android.content.Context
import android.media.AudioManager
import com.hyc.m3u8downloader.MainApplication

class AudioVolumeController {
    private val manager: AudioManager = MainApplication.instance.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getMaxVolume() = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    fun getCurrentVolume() = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
    fun setVolume(size: Int) {
//        var target: Int
//        val max = getMaxVolume()
//        target = when {
//            size <= 0 -> 0
//            size >= 100 -> max
//            else -> size * max / 100
//        }
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, size, AudioManager.FLAG_PLAY_SOUND)
    }

    companion object {
        @Volatile
        private var INSTANCE: AudioVolumeController? = null

        fun getInstance(): AudioVolumeController = INSTANCE ?: synchronized(this) {
            INSTANCE ?: AudioVolumeController().also { INSTANCE = it }
        }
    }

}