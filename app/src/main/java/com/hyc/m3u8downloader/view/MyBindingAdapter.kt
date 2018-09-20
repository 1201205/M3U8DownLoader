package com.hyc.m3u8downloader.view

import android.databinding.BindingAdapter
import android.text.TextUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hyc.m3u8downloader.R

@BindingAdapter("state")
fun changeState(view: TextView, state: Int) {
    when (state) {
        0 -> view.text = "初始化"
        1 -> view.text = "下载中"
        2 -> view.text = "已暂停"
    }
}

@BindingAdapter("changeBackground")
fun changeBackground(button: Button, state: Int) {
    when (state) {
        0 -> button.setBackgroundResource(R.mipmap.item_waiting)
        1 -> button.setBackgroundResource(R.mipmap.pause)
        2 -> button.setBackgroundResource(R.mipmap.play)
    }
}

@BindingAdapter(value = ["downloadCount", "totalCount"], requireAll = true)
fun updateProgress(view: TextView, downloadCount: Int, totalCount: Int) {
    view.text = "已下载$downloadCount/{$totalCount}个文件"
}

@BindingAdapter("picPath")
fun showPic(view: ImageView, path: String?) {
    if (TextUtils.isEmpty(path)) {
        return
    }
    Glide.with(view).load(path).into(view)
}