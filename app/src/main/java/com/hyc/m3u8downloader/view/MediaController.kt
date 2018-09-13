package com.hyc.m3u8downloader.view

interface MediaController {
    fun createNewMedia(url:String,name:String)
    fun pauseAll()
    fun resumeAll()
}