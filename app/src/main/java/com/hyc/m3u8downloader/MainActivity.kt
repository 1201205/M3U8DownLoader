package com.hyc.m3u8downloader

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.hyc.m3u8downloader.model.MediaItem
import com.hyc.m3u8downloader.model.MyDatabase
import com.hyc.m3u8downloader.model.TSItem
import com.hyc.m3u8downloader.utils.CMDUtil
import com.hyc.m3u8downloader.view.MainAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

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
//        btEnter = findViewById(R.id.bt_enter)
//        etUrl = findViewById(R.id.et_url)
//        btEnter.setOnClickListener {
//            FileDownloader().downLoad(etUrl.text.toString(), object : DownloadCallBack {
//                override fun onDownloadSuccess(url: String) {
//                }
//
//                override fun onDownloadFailed(url: String) {
//                }
//            })
//        }
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
        var list2: ArrayList<MediaItem> = ArrayList()
        for (i in 1..30) {
            var item = MediaItem()
            item.mp4Path = "aaaa" + i
            item.name = "aaaa" + i
            list2.add(item)
        }
        findViewById<RecyclerView>(R.id.rv_content).let {
            it.adapter = MainAdapter(list2)
            var manager = LinearLayoutManager(this)
            it.layoutManager = manager
            it.postDelayed({        CMDUtil.instance.exeThumb("/sdcard/m3u8/1.ts","/sdcard/m3u8/23.jpg",200,200,5f)
            },2000)
        }

        findViewById<FloatingActionButton>(R.id.fab_menu).setOnClickListener { showBottomMenu() }
//        Thread(object :Runnable{
//            override fun run() {
//              Log.d("hyc-db",MyDatabase.getInstance().getMediaItemDao().loadLastItem().toString())
//            }
//        }).start()
    }

    private fun showBottomMenu() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_menu, null)
        dialog.setContentView(view)
        dialog.show()
    }
}
