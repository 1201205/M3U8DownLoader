package com.hyc.m3u8downloader.view

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AlertDialog
import android.widget.EditText
import android.widget.TextView
import com.hyc.m3u8downloader.R

class MenuDialog(context: Context,controller: MediaController) : BottomSheetDialog(context) {
  val mController= controller
    init {
        val view = layoutInflater.inflate(R.layout.dialog_menu, null)
        setContentView(view)
        findViewById<TextView>(R.id.tv_new)!!.setOnClickListener {
            MenuDialog@ this.dismiss()
            showAddDialog()
        }
    }

    private fun showAddDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("新建下载")
        builder.setMessage("输入m3u8下载地址")
        val editText=EditText(context)
        builder.setView(editText)
        builder.setNegativeButton("取消", null)
        builder.setPositiveButton("确定") { _, _ ->
//            mController.createNewMedia(editText.text.toString(),"")
        }
        builder.show()

    }
}