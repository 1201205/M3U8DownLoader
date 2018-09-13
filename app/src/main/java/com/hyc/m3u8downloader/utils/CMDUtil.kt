package com.hyc.m3u8downloader.utils

import android.text.TextUtils
import android.util.Log
import com.hyc.m3u8downloader.MainApplication
import java.io.File
import java.io.FileOutputStream

class CMDUtil private constructor() {
    private val fileName = "ffmpeg"
    private val suffix = "/"
    var hasCMDFile: Boolean by Sp("hasCMDFile", false)
    private var absFilePath: String? = null

    init {
        var file = File(MainApplication.instance.filesDir.absolutePath + suffix + fileName)
        if (!file.exists() || !hasCMDFile) {
            try {
                val inputStream = MainApplication.instance.assets.open(fileName)
                val out = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var n: Int = inputStream.read(buffer)
                while (n != -1) {
                    out.write(buffer, 0, n)
                    n = inputStream.read(buffer)
                }
                out.close()
                hasCMDFile = true
            } catch (e: Exception) {
                hasCMDFile = false
            }
        }
        if (hasCMDFile) {
            file.setExecutable(true)
            absFilePath = file.absolutePath
            Runtime.getRuntime().exec("chmod 777 $absFilePath")
        }
    }

    companion object {
        val instance: CMDUtil by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CMDUtil()
        }
    }

    fun execute(cmd: String) {
        Runtime.getRuntime().exec(cmd)
    }

    fun canUseCMD() = !TextUtils.isEmpty(absFilePath)
    fun executeMerge(configPath: String, targetPath: String) {
        Runtime.getRuntime().exec("$absFilePath -f concat -i $configPath -c copy $targetPath -y ")
        Log.e("cmd", "$absFilePath -f concat -i $configPath -c copy $targetPath -y ")
    }

    fun exeThumb(mediaPath: String, targetPath: String, width: Int, height: Int, time: Float) {
        Log.e("hyc-cmd", "start--" + absFilePath)
        Runtime.getRuntime().exec("$absFilePath -i $mediaPath -vframes 1 -y -f image2 -t 0.5 -s $width*$height $targetPath")
        Log.e("hyc-cmd", "end--")
    }
}