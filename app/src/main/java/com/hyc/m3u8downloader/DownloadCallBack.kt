package com.hyc.m3u8downloader

interface DownloadCallBack {
    fun onDownloadSuccess(url:String)
    fun onDownloadFailed(url: String)
}