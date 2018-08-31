package com.hyc.m3u8downloader

interface ParseCallBack {
    fun onParseSuccess(list:List<String>)
    fun onParseFailed(errorLog:String)
    fun onNeedDownLoad(url:String)
}