package com.hyc.m3u8downloader

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private val SDCARD_PERMISSION_R = Manifest.permission.READ_EXTERNAL_STORAGE
    private val SDCARD_PERMISSION_W = Manifest.permission.WRITE_EXTERNAL_STORAGE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(SDCARD_PERMISSION_R, SDCARD_PERMISSION_W), 100)
//        FileDownloader().downLoad("http://gncdn.wb699.com/20180831/Avl99908/index.m3u8","/sdcard/1.m3u8",object :DownloadCallBack{
//            override fun onDownloadSuccess(url: String) {
//            }
//
//            override fun onDownloadFailed(url: String) {
//            }
//        })
        var inputStream = assets.open("ffmpeg")
        var out = FileOutputStream(File(filesDir.absolutePath + "/ffmpeg"))
        val buffer = ByteArray(1024)
        var n: Int = inputStream.read(buffer)
        while (n != -1) {
            out.write(buffer, 0, n)
            n = inputStream.read(buffer)
        }
        out.close()
        inputStream.close()
        var file = File(filesDir.absolutePath + "/ffmpeg")
        file.setExecutable(true)
        Runtime.getRuntime().exec("chmod 777 " + file.absolutePath)
////        ffmpeg
//        Runtime.getRuntime().exec("su")
        Log.e("hyc-start", System.currentTimeMillis().toString())
        val process = Runtime.getRuntime().exec(file.absolutePath + " -f concat -i /sdcard/1/1.txt -c copy /sdcard/7.avi -y ")
        Log.e("hyc-end", System.currentTimeMillis().toString())


    }
}
