package com.hyc.m3u8downloader

import android.Manifest
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.hyc.m3u8downloader.databinding.ActivityMainBinding
import com.hyc.m3u8downloader.model.ItemChangeEvent
import com.hyc.m3u8downloader.model.MediaItem
import com.hyc.m3u8downloader.model.MyDatabase
import com.hyc.m3u8downloader.model.TSItem
import com.hyc.m3u8downloader.utils.CMDUtil
import com.hyc.m3u8downloader.utils.ItemDelegate
import com.hyc.m3u8downloader.utils.MD5Util
import com.hyc.m3u8downloader.utils.rootPath
import com.hyc.m3u8downloader.view.MainAdapter
import com.hyc.m3u8downloader.view.MediaController
import com.hyc.m3u8downloader.view.MenuDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), MediaController {
    override fun onFabClicked(view: View) {
        if (menuShowing) {
            closeMenu()
        } else {
            showMenu()
        }
    }

    override fun onDeleteAllClicked(view: View) {
        Log.e("hyc-fab", "deleteAll")
    }

    override fun onCreateNewMediaClicked(view: View) {
        Log.e("hyc-fab", "createNewMedia")

//        val item = MediaItem()
//        item.url = url
//        item.parentPath = rootPath + MD5Util.crypt(item.url)
//        item.state = 0
//        mAdapter.addItem(item)
//        FileDownloader().download(item, object : DownloadCallBack {
//            override fun onDownloadSuccess(url: String) {
//            }
//
//            override fun onDownloadFailed(url: String) {
//            }
//        })
    }

    override fun onPauseAllClicked(view: View) {
        Log.e("hyc-fab", "pauseAll")

    }

    override fun onResumeAllClicked(view: View) {
        Log.e("hyc-fab", "resumeAll")
    }

    lateinit var item: MediaItem
    private val SDCARD_PERMISSION_R = Manifest.permission.READ_EXTERNAL_STORAGE
    private val SDCARD_PERMISSION_W = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val mRequestCode = 100
    private lateinit var mAdapter: MainAdapter
    private lateinit var mBinding: ActivityMainBinding
    private val delayTime = 80L
    private var menuShowing = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.controller = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(SDCARD_PERMISSION_R, SDCARD_PERMISSION_W), 100)
        }
//        Log.e("hyc-kk", kk.test.toString() + "---")
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
        item = MediaItem()
        item.name = "1"
//        MyDatabase.getInstance().getMediaItemDao().insertMedia(item)
//        item.id=1
//        var list = ArrayList<TSItem>()
//        for (i in 1..10) {
//            var tsItem = TSItem()
////            tsItem.mediaId=1
//            tsItem.path = "aaaa" + i
//            list.add(tsItem)
//        }
//        Observable.create<Any> { MyDatabase.getInstance().getMediaItemDao().insertMedia(item) }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe({
//            Log.e("hyc--oo", "----" + item.id)
//        })
//        MyDatabase.getInstance().getMediaItemDao().loadLastItem().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe { it ->
//            it?.let {
//                Log.e("hyc--oo", it.toString())
//            }
//        }
        findViewById<RecyclerView>(R.id.rv_content).let {
            var list = java.util.ArrayList<MediaItem>()
            mAdapter = MainAdapter(list)
            it.adapter = mAdapter
            var manager = LinearLayoutManager(this)
            it.layoutManager = manager
            (it.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChange(event: ItemChangeEvent) {
        mAdapter.notifyItemChanged(event.index)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (mRequestCode == requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "抱歉，未获取到读写权限，无法使用该应用", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    }

    private fun showMenu() {
        menuShowing = true
        mBinding.fabMenu.animate().rotation(45f).setDuration(300).setInterpolator(OvershootInterpolator()).start()
        mBinding.llStartAll.animate().setDuration(300).alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(delayTime).start()
        mBinding.llCreate.animate().setDuration(300).alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(delayTime*3).start()
        mBinding.llDeleteAll.animate().setDuration(300).alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(delayTime * 2).start()
        mBinding.llPauseAll.animate().setDuration(300).alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(0).start()
    }

    private fun closeMenu() {
        menuShowing = false
        mBinding.fabMenu.animate().rotation(0f).setDuration(300).setInterpolator(OvershootInterpolator()).start()
        mBinding.llStartAll.animate().setDuration(300).alpha(0f).scaleX(0f).scaleY(0f).setStartDelay(delayTime * 2).start()
        mBinding.llCreate.animate().setDuration(300).alpha(0f).scaleX(0f).scaleY(0f).setStartDelay(0).start()
        mBinding.llDeleteAll.animate().setDuration(300).alpha(0f).scaleX(0f).scaleY(0f).setStartDelay(delayTime).start()
        mBinding.llPauseAll.animate().setDuration(300).alpha(0f).scaleX(0f).scaleY(0f).setStartDelay(delayTime * 3).start()

    }

}
