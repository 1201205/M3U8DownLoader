package com.hyc.m3u8downloader

import android.util.Log
import okhttp3.Call
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class M3u8Downloader(private val path: String, private val lock: MultLock, private val call: Call) : Runnable {

    override fun run() {
        call.request().url().toString()
        call.execute().let { response ->
            var inputStream: InputStream
            var buf = ByteArray(2048)
            var fos: FileOutputStream
            try {
                response.body()?.let { rs ->
                    inputStream = rs.byteStream()
                    val total = rs.contentLength()
                    var file = File(path)
                    fos = FileOutputStream(file)
                    var sum = 0L
                    var len = 0
                    while (inputStream.read(buf).apply { len = this } != -1) {
                        fos.write(buf, 0, len)
                        sum += len
                        Log.e("hyc-progress", "total:$total---current:$sum+++++$len")
                    }
                    Log.e("hyc-progress", "total:$total---current:$sum+++++$len")
                    fos.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        lock.unlock()
    }

    private fun getFilePath(url: String): String {
        return ""
    }

//    interface DownloadCallback{
//        fun onFileDownloadSuccess()
//    }
}