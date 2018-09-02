package com.hyc.m3u8downloader

import java.util.ArrayList

interface IMediaManager {
    /**
     * 整体流程
     * 1.用户输入m3u8地址
     * 2.下载m3u8文件，并解析下载地址
     * 3.根据m3u8地址去生成一个文件夹，并根据相关规则去检查是否还有必要去重新下载文件,（目前想法是以文件后缀为准）
     * 4.按照提交的下载地址去下载，下载成功一个就通知一次
     * 5.所有下载完成，生成一个file.txt文件去执行ffmpeg合成命令
     * 是否需要数据库？url与文件存在对应关系，展示之前的内容需要
     * m3u8文件是否需要缓存  如果需要拿就直接将3和2的步骤换一下
     *
     * 对外的接口暴露
     * 1.提供一个输入m3u8，也就是总入口
     * 2.二次进入展示下载进度（初步以文件为准）
     * 3.删除已经合成的文件或者正在下载的整体文件
     * 4.获取文件列表
     * 5.一些设置接口，比如线程数量，同时下载数量
     *
     */

    fun downloadM3u8(url:String)//重新下载或者继续下载，内部做判断
    fun getHistoryList():ArrayList<MediaItem>
    fun deleteM3u8(url: String)
    fun getThreadCount():Int
    fun getFileCount():Int
    fun setThreadCount(count:Int)
    fun setFileCount(count: Int)
}