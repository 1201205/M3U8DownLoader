package com.hyc.m3u8downloader

import java.util.ArrayList

interface IMediaDownLoader {
    fun startDownload(url:String,callBack: DownloadCallBack)
    fun stopDownload()
}