package com.hyc.m3u8downloader

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private val SDCARD_PERMISSION_R = Manifest.permission.READ_EXTERNAL_STORAGE
    private val SDCARD_PERMISSION_W = Manifest.permission.WRITE_EXTERNAL_STORAGE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf( SDCARD_PERMISSION_R, SDCARD_PERMISSION_W),100)
        FileDownloader().downLoad("http://gncdn.wb699.com/20180831/Avl99908/index.m3u8","/sdcard/1.m3u8",object :DownloadCallBack{
            override fun onDownloadSuccess(url: String) {
            }

            override fun onDownloadFailed(url: String) {
            }
        })
    }
}
