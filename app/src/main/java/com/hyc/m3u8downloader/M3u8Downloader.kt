package com.hyc.m3u8downloader

import android.util.Log
import com.hyc.m3u8downloader.model.TSItem
import okhttp3.Call
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class M3u8Downloader(private val tsFile: TSItem, private val lock: DownloadCallback, private val call: Call) : Runnable {

    override fun run() {
        try {
            val path=tsFile.path
            var index: Int = tsFile.index!!
            call.execute().let { response ->
                var inputStream: InputStream
                var buf = ByteArray(1024*4)
                var fos: FileOutputStream? = null
                try {
                    response.body()?.let { rs ->
                        inputStream = rs.byteStream()
                        val total = rs.contentLength()
                        tsFile.total = total
                        lock.onGetContentLength(tsFile)
                        var file = File(path)
                        fos = FileOutputStream(file)
                        var sum = 0L
                        var len = 0
                        while (inputStream.read(buf).apply { len = this } != -1) {
                            fos!!.write(buf, 0, len)
                            sum += len
                        }
                        Log.e("hyc-progress", "total:$total---current:$sum+++++$len+++++$index")
                        lock.onFileDownloadSuccess(tsFile)
                    }
                } catch (e: Exception) {
                    lock.onFileDownloadFailed(tsFile)
                    Log.e("hyc-progress", "need ReDownload )" + call.request().url().toString())
                    e.printStackTrace()
                }
                fos?.flush()
            }
        } catch (e: Exception) {
            Log.e("hyc-progress", "need ReDownload --outer )" + call.request().url().toString())
            lock.onFileDownloadFailed(tsFile)
            e.printStackTrace()
        }

    }

    private fun getFilePath(url: String): String {
        return ""
    }

    interface DownloadCallback {
        fun onFileDownloadSuccess(tsFile: TSItem)
        fun onGetContentLength(tsFile: TSItem)
        fun onFileDownloadFailed(tsFile: TSItem)
    }
}