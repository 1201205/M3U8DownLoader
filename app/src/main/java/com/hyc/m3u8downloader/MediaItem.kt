package com.hyc.m3u8downloader

import java.util.ArrayList

data class MediaItem(var url: String, var path: String, var fragmentList: ArrayList<String>, var downLoadList: ArrayList<String>) {
//    var url:String?=null
//    var fragmentList:ArrayList<String>
}