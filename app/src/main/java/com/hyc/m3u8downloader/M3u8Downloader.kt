package com.hyc.m3u8downloader

import android.util.Log
import okhttp3.Call
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class M3u8Downloader(var index: Int, private val path: String, private val lock: DownloadCallback, private val call: Call) : Runnable {

    override fun run() {

        call.execute().let { response ->
            var inputStream: InputStream
            var buf = ByteArray(2048)
            var fos: FileOutputStream? = null
            try {
                response.body()?.let { rs ->
                    inputStream = rs.byteStream()
                    val total = rs.contentLength()
                    lock.onGetContentLength(index, total)
                    var file = File(path)
                    fos = FileOutputStream(file)
                    var sum = 0L
                    var len = 0
                    while (inputStream.read(buf).apply { len = this } != -1) {
                        fos!!.write(buf, 0, len)
                        sum += len
                    }
                    Log.e("hyc-progress", "total:$total---current:$sum+++++$len+++++$index")
                    lock.onFileDownloadSuccess(call.request().url().toString())
                }
            } catch (e: Exception) {
                lock.onFileDownloadFailed(call.request().url().toString(), index)
                Log.e("hyc-progress", "need ReDownload )" + call.request().url().toString())
                e.printStackTrace()
            }
            fos?.flush()
        }
    }

    private fun getFilePath(url: String): String {
        return ""
    }

    interface DownloadCallback {
        fun onFileDownloadSuccess(url: String)
        fun onGetContentLength(index: Int, length: Long)
        fun onFileDownloadFailed(url: String, index: Int)
    }
}