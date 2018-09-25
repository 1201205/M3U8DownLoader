package com.hyc.m3u8downloader.view

import android.app.Activity
import android.content.DialogInterface
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.EditText
import com.hyc.m3u8downloader.utils.NetStateChangeReceiver

fun showAddDialog(activity: Activity, listener: GetTextListener) {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("新建下载")
    builder.setMessage("输入m3u8下载地址")
    val editText = EditText(activity)
    builder.setView(editText)
    builder.setNegativeButton("取消", null)
    builder.setPositiveButton("确定") { _, _ ->
        listener.onGetText(editText.text.toString())
    }
    builder.show()
}

fun showSpaceNotEnoughDialog(activity: Activity) {
    Log.e("hyc-fab", "current space   ${Environment.getExternalStorageDirectory().freeSpace / 1024 / 1024}m  not enough")
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("存储空间不足")
    builder.setMessage("设备目前存储空间不足，无法创建下载，请清理后重试!")
    builder.setPositiveButton("确定", null)
}

fun showDeleteAllDialog(activity: Activity, listener: PositiveClickListener) {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("删除全部")
    builder.setMessage("是否全部删除，同时将会删除已下载的媒体文件")
    builder.setNegativeButton("取消", null)
    builder.setPositiveButton("确定") { _, _ ->
        listener.onPositiveClicked()
    }
    builder.show()
}

fun showNotWifiDialog(activity: Activity, listener: PositiveClickListener) {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("提示")
    builder.setMessage("当前使用非WIFI连接，下载会消耗大量数据流量！")
    builder.setNegativeButton("取消", null)
    builder.setPositiveButton("下载") { _, _ ->
        listener.onPositiveClicked()
    }
    builder.setNeutralButton("下载并不再提示") { _, _ ->
        listener.onPositiveClicked()
        NetStateChangeReceiver.getInstance().ignoreNetState = true
    }
    builder.show()
}

fun showReDownloadDialog(activity: Activity, listener: PositiveClickListener) {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("提示")
    builder.setMessage("未找到合成的mp4文件，是否重新下载")
    builder.setNegativeButton("取消", null)
    builder.setPositiveButton("确定") { _, _ ->
        listener.onPositiveClicked()
    }
    builder.show()
}

fun showDeleteItemDialog(activity: Activity, listener: PositiveClickListener){
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("提示")
    builder.setMessage("是否删除该资源？")
    builder.setNegativeButton("取消", null)
    builder.setPositiveButton("确定") { _, _ ->
        listener.onPositiveClicked()
    }
    builder.show()
}

interface GetTextListener {
    fun onGetText(url: String)
}

interface PositiveClickListener {
    fun onPositiveClicked()
}
