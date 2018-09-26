package com.hyc.m3u8downloader

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.text.TextUtils
import android.util.Log
import com.hyc.m3u8downloader.model.MediaItem
import com.hyc.m3u8downloader.model.MediaItemDao
import com.hyc.m3u8downloader.model.MyDatabase
import com.hyc.m3u8downloader.model.TSItem
import com.hyc.m3u8downloader.utils.MD5Util
import com.hyc.m3u8downloader.utils.rootPath
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor

class FileDownloader(client: OkHttpClient, executors: ExecutorService, lock: MultLock) {
    /**
     * 如何暂停下载？？
     * 1.确定下载过程中的当前状态：有下载m3u8和解析文件和下载ts文件3种状态
     * 2.针对每个状态做出处理：下载m3u8就直接取消，解析文件就等待解析完毕，下载ts就发送消息结束下载循环
     */
    private var mClient: OkHttpClient = client
    private var multLock = lock
    private var mExecutors = executors
    private var mRedirect = 0
    private val INIT = 0
    private val DOWNLOADING_M3U8 = 1
    private val PARSING = 2
    private val DOWNLOADING_TS = 3
    private var currentState = INIT
    private var stopped = false
    var mItem: MutableLiveData<MediaItem>? = null
    private var downloader: MediaDownloader? = null
    @SuppressLint("SdCardPath")
    fun downLoad(url: String, callBack: DownloadCallBack) {
        File(mItem!!.value!!.parentPath).mkdirs()
        val path = getFilePath()
        val file = File(path)
        file.deleteOnExit()
        file.createNewFile()
        Log.e("hyc-downloader", "first file---$path")
        downLoad(url, path, callBack)
        mRedirect++
    }

    fun download(item: MutableLiveData<MediaItem>, callBack: DownloadCallBack) {
        mItem = item
        item.value?.let {
            if (it.list == null) {
                if (TextUtils.isEmpty(it.url)) {
                    return
                }
                mItem = item
                downLoad(it.url!!, callBack)
            } else {
                it.state = DownloadState.DOWNLOADING
                mItem!!.postValue(it)
                currentState = DOWNLOADING_TS
                downloader = MediaDownloader().withClient(mClient).withExecutor(mExecutors).withLock(multLock)
                downloader!!.download(mItem!!, callBack)

            }
        }

    }

    private fun getFilePath(): String = "${mItem!!.value!!.parentPath}/$mRedirect.m3u8"

    private fun onDownloadSuccess() {
        Log.e("file-downloader", "onDownloadSuccess")
//        for (path in mFileArray) {
//            deleteFile(File(path))
//        }
    }

    fun isThisDownloading(item: MutableLiveData<MediaItem>): Boolean {
        return mItem == item
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


    fun stopDownload() {
        when (currentState) {
            DOWNLOADING_M3U8, PARSING -> stopped = true
            DOWNLOADING_TS -> downloader?.let { it.stopDownload() }
        }
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
                            while (!stopped && inputStream.read(buf).apply { len = this } != -1) {
                                fos.write(buf, 0, len)
                                sum += len
                                Log.e("hyc-progress", "total:$total---current:$sum+++++$len")
                            }
                            Log.e("hyc-progress", "total:$total---current:$sum+++++$len")
                            fos.flush()
                            currentState = PARSING
                            M3u8FileParser().parse(mItem!!.value!!.id!!, url, file, object : ParseCallBack {
                                override fun onParseSuccess(list: List<TSItem>) {
                                    mItem?.value?.let { item ->
                                        if (stopped) {
                                            item.state = DownloadState.STOPPED
                                        } else {
                                            item.state = DownloadState.DOWNLOADING
                                        }
                                        MediaItemDao.insertTSItems(list)
                                        mItem!!.postValue(item)
                                        if (stopped) {
                                            return
                                        }
                                        currentState = DOWNLOADING_TS
                                        downloader = MediaDownloader().withClient(mClient).withExecutor(mExecutors).withLock(multLock)
                                        downloader!!.download(mItem!!, callBack)
                                    }
                                }

                                override fun onNeedDownLoad(url: String) {
                                    currentState = DOWNLOADING_M3U8
                                    if (stopped) {
                                        return
                                    }
                                    downLoad(url, callBack)
                                }

                                override fun onParseFailed(errorLog: String) {
                                    callBack.onDownloadFailed(mItem!!)
                                }
                            })
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callBack.onDownloadFailed(mItem!!)
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