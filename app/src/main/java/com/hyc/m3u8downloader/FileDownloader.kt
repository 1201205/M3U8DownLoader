package com.hyc.m3u8downloader

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import com.hyc.m3u8downloader.model.MediaItem
import com.hyc.m3u8downloader.model.MediaItemDao
import com.hyc.m3u8downloader.model.MyDatabase
import com.hyc.m3u8downloader.utils.MD5Util
import com.hyc.m3u8downloader.utils.rootPath
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FileDownloader {
    private var mClient: OkHttpClient = OkHttpClient()
    private var mRedirect = 0
    var mItem: MediaItem? = null
    @SuppressLint("SdCardPath")
    fun downLoad(url: String, callBack: DownloadCallBack) {
        File(mItem!!.parentPath).mkdirs()
        val path = getFilePath()
        val file = File(path)
        file.deleteOnExit()
        file.createNewFile()
        Log.e("hyc-downloader", "first file---$path")
        downLoad(url, path, callBack)
        mRedirect++
    }

    fun download(item: MediaItem, callBack: DownloadCallBack) {
        if (TextUtils.isEmpty(item.url)) {
            return
        }
        mItem = item
        mItem!!.parentPath = rootPath + MD5Util.crypt(item.url)
        MediaItemDao.getIDAndInsert(mItem!!)
        downLoad(item.url!!, callBack)
    }

    private fun getFilePath(): String = "${mItem!!.parentPath}/$mRedirect.m3u8"

    private fun onDownloadSuccess() {
        Log.e("file-downloader", "onDownloadSuccess")
//        for (path in mFileArray) {
//            deleteFile(File(path))
//        }
    }

    private fun deleteFile(file: File) {
        //目前先订为mp4
//        if (file.isFile && !file.absolutePath.endsWith(".mp4")) {
//            file.delete()
//        } else if (file.isDirectory) {
//            file.listFiles().forEach {
//                deleteFile(it)
//            }
//        }
    }

    private fun downLoad(url: String, path: String, callBack: DownloadCallBack) {
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
                                    downLoad(url, callBack)
                                }

                                override fun onParseFailed(errorLog: String) {
                                }

                                override fun onParseSuccess(list: List<String>) {
                                    mItem?.let {item->
                                        item.state = 1
                                        item.tsUrls = list
                                        item.notifyChanged()
                                        var downloader = MediaDownloader()
                                        downloader.download(item, File(path).parentFile.absolutePath, object : MediaMergeCallback {
                                            override fun onSuccess() {
                                                onDownloadSuccess()
                                            }

                                            override fun onFailed() {
                                            }
                                        })
                                    }

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

    interface MediaMergeCallback {
        fun onSuccess()
        fun onFailed()
    }
}