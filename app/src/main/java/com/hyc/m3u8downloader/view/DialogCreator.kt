package com.hyc.m3u8downloader.view

import android.app.Activity
import android.content.DialogInterface
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.hyc.m3u8downloader.R
import com.hyc.m3u8downloader.utils.Config
import com.hyc.m3u8downloader.utils.NetStateChangeReceiver

fun showAddDialog(activity: Activity, listener: GetTextListener) {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("新建下载")
    val viewGroup = LayoutInflater.from(activity).inflate(R.layout.dialog_input, null)
    val etName = viewGroup.findViewById<EditText>(R.id.et_name)
    val etUrl = viewGroup.findViewById<EditText>(R.id.et_url)

    builder.setView(viewGroup)
    builder.setNegativeButton("取消", null)
    builder.setPositiveButton("确定") { _, _ ->
        listener.onGetText(etUrl.text.toString(), etName.text.toString())
    }
    builder.show()
}

fun showSpaceNotEnoughDialog(activity: Activity) {
    Log.e("hyc-fab", "current space   ${Environment.getExternalStorageDirectory().freeSpace / 1024 / 1024}m  not enough")
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("存储空间不足")
    builder.setMessage("设备目前存储空间不足，无法创建下载，请清理后重试!")
    builder.setPositiveButton("确定", null)
    builder.show()
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
    builder.setTitle("警告")
    builder.setMessage("当前使用非WIFI连接，下载会消耗大量数据流量！")
    builder.setNegativeButton("取消", null)
    builder.setPositiveButton("下载") { _, _ ->
        listener.onPositiveClicked()
    }
    builder.setNeutralButton("下载并不再提示") { _, _ ->
        listener.onPositiveClicked()
        Config.dataWork = true
    }
    builder.show()
}

fun show4GDialog(activity: Activity, listener: PositiveClickListener, negativeClickListener: NegativeClickListener) {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("警告")
    builder.setMessage("开启此选项，将使用非WIFI网络下载，会消耗大量数据流量！")
    builder.setNegativeButton("取消") { _, _ ->
        negativeClickListener.onNegativeClicked()
    }
    builder.setPositiveButton("我知道了") { _, _ ->
        listener.onPositiveClicked()
    }
    builder.show()
}

fun showForegroundDialog(activity: Activity, listener: PositiveClickListener, negativeClickListener: NegativeClickListener) {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("提示")
    builder.setMessage("App将弹出通知，从而在一定程度上降低App被系统回收的风险，提升App在后台下载过程中的稳定性，可能需要进行手动授权")
    builder.setNegativeButton("取消") { _, _ ->
        negativeClickListener.onNegativeClicked()
    }
    builder.setPositiveButton("确定") { _, _ ->
        listener.onPositiveClicked()
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

fun showDeleteItemDialog(activity: Activity, listener: PositiveClickListener) {
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
    fun onGetText(url: String, name: String)
}

interface PositiveClickListener {
    fun onPositiveClicked()
}

interface NegativeClickListener {
    fun onNegativeClicked()
}