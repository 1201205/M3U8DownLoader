package com.hyc.m3u8downloader.view

import android.databinding.BindingAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hyc.m3u8downloader.DownloadState
import com.hyc.m3u8downloader.R

@BindingAdapter("state")
fun changeState(view: TextView, state: DownloadState) {
    when (state) {
        DownloadState.WAITING -> view.text = "等待下载"
        DownloadState.DOWNLOADING -> view.text = "下载中"
        DownloadState.STOPPED -> view.text = "已暂停"
        DownloadState.SUCCESS -> view.text = "已完成"
        DownloadState.MERGING -> view.text = "文件合成中"
        DownloadState.FAILED -> view.text = "下载失败"
        DownloadState.BAD_URL -> view.text = "无效链接，请检查后重试"
    }
}

@BindingAdapter("changeBackground")
fun changeBackground(button: Button, state: DownloadState) {
    when (state) {
        DownloadState.WAITING -> button.setBackgroundResource(0)
        DownloadState.DOWNLOADING -> button.setBackgroundResource(R.mipmap.pause)
        DownloadState.STOPPED, DownloadState.SUCCESS -> button.setBackgroundResource(R.mipmap.play)
    }
}

@BindingAdapter(value = ["downloadCount", "totalCount", "state"], requireAll = true)
fun updateProgress(view: TextView, downloadCount: Int, totalCount: Int, state: DownloadState) {
    when (state) {
        DownloadState.DOWNLOADING ->
            if (totalCount > 0) {
                view.text = "已下载 $downloadCount/$totalCount"
            } else {
                view.text = "正在解析初始文件"
            }
        else -> {
            view.text=""
        }
    }
}

@BindingAdapter("picPath")
fun showPic(view: ImageView, path: String?) {
    GlideApp.with(view).load(path).centerCrop().error(R.mipmap.ic_launcher_round).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.mipmap.ic_launcher_round).into(view)
}