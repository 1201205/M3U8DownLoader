package com.hyc.m3u8downloader

import android.annotation.SuppressLint
import android.util.Log
import com.hyc.m3u8downloader.utils.MD5Util
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FileDownloader {
    var mClient: OkHttpClient = OkHttpClient()
    @SuppressLint("SdCardPath")
    fun downLoad(url: String, callBack: DownloadCallBack) {
        val parent="/sdcard/m3u8/" + MD5Util.crypt(url)
        File(parent).mkdirs()

        val path = parent +"/0.m3u8"
        val file = File(path)
            file.deleteOnExit()
        file.createNewFile()
        Log.e("hyc-downloader", "first file---" + path)
        downLoad(url, path, callBack)
    }

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
                                    downLoad(url, object : DownloadCallBack {
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
                                    downloader.download(list, File(path).parentFile.absolutePath)
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