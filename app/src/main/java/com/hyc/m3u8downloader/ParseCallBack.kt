package com.hyc.m3u8downloader

import com.hyc.m3u8downloader.model.TSItem

interface ParseCallBack {
    fun onParseSuccess(list:List<TSItem>)
    fun onParseFailed(errorLog:String)
    fun onNeedDownLoad(url:String)
}