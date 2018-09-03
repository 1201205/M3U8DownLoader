package com.hyc.m3u8downloader

import android.util.Log
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FileDownloader {
    var mClient: OkHttpClient = OkHttpClient()

    fun downLoad(url: String, path: String, callBack: DownloadCallBack) {
        val request = Request.Builder().url(url).build()
        mClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                var inputStream: InputStream
                var buf = ByteArray(2048)
                var fos: FileOutputStream
                try {
                    response?.let { rs ->
                        rs.body()?.let {
                            inputStream = it.byteStream()
                            val total = it.contentLength()
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
                            callBack.onDownloadSuccess(url)
                            M3u8FileParser().parse(url, file, object : ParseCallBack {
                                override fun onNeedDownLoad(url: String) {
                                    downLoad(url, "/sdcard/2.m3u8", object : DownloadCallBack {
                                        override fun onDownloadSuccess(url: String) {
                                        }

                                        override fun onDownloadFailed(url: String) {
                                        }
                                    })
                                }

                                override fun onParseFailed(errorLog: String) {
                                }

                                override fun onParseSuccess(list: List<String>) {
                                    var downloader = MediaDownloader()
                                    downloader.download(list)
                                }
                            })
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
            }
        })
    }
}