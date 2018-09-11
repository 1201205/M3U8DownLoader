package com.hyc.m3u8downloader

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.hyc.m3u8downloader.model.MediaItem
import com.hyc.m3u8downloader.model.MyDatabase
import com.hyc.m3u8downloader.model.TSItem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*

class MainActivity : AppCompatActivity() {

    private val SDCARD_PERMISSION_R = Manifest.permission.READ_EXTERNAL_STORAGE
    private val SDCARD_PERMISSION_W = Manifest.permission.WRITE_EXTERNAL_STORAGE
    lateinit var etUrl: EditText
    lateinit var btEnter: Button

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
        btEnter = findViewById(R.id.bt_enter)
        etUrl = findViewById(R.id.et_url)
        btEnter.setOnClickListener {
            FileDownloader().downLoad(etUrl.text.toString(), object : DownloadCallBack {
                override fun onDownloadSuccess(url: String) {
                }

                override fun onDownloadFailed(url: String) {
                }
            })
        }
        var item = MediaItem()
        item.name = "1"
//        item.id=1
        var list = ArrayList<TSItem>()
        for (i in 1..10) {
            var tsItem = TSItem()
//            tsItem.mediaId=1
            tsItem.path = "aaaa" + i
            list.add(tsItem)
        }
        Observable.create<Any> { MyDatabase.getInstance().getMediaItemDao().insertMedia(item) }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe()
        MyDatabase.getInstance().getMediaItemDao().loadLastItem().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe { it ->
            it?.let {
                Log.e("hyc--oo", it.toString())
            }
        }
//        Thread(object :Runnable{
//            override fun run() {
//              Log.d("hyc-db",MyDatabase.getInstance().getMediaItemDao().loadLastItem().toString())
//            }
//        }).start()
    }
}
