package com.hyc.m3u8downloader.model

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.hyc.m3u8downloader.DownloadManager
import com.hyc.m3u8downloader.MainApplication
import com.hyc.m3u8downloader.view.MainAdapter2
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainViewModel(app: Application) : AndroidViewModel(app) {
    var adapter: MutableLiveData<MainAdapter2> = MutableLiveData()
    var item: MutableLiveData<MediaItem> = MutableLiveData()
    fun loadingFormDB(context: Context) {
        Observable.create(ObservableOnSubscribe<MainAdapter2> { emitter ->
            emitter.onNext(MainAdapter2(DownloadManager.getInstance().getAllMedia(), context))
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            adapter.postValue(it)
        }
//        DownloadManager.getInstance().getAllMedia().map { t ->
//            var target = ArrayList<MutableLiveData<MediaItem>>()
//            if (t != null && !t.isEmpty()) {
//                for (item in t) {
//                    var media = item.mediaItem
//                    media!!.list = item.tsFiles
//                    var data = MyLiveData()
//                    data.postValue(media)
//                    target.add(data)
//                }
//            }
//            MainAdapter2(target, context)
//        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe {
//            adapter.postValue(it)
//        }
    }

    fun deleteAll() {
        DownloadManager.getInstance().deleteAll()
        adapter.value!!.notifyDataSetChanged()
    }

    fun pauseAll() {
        DownloadManager.getInstance().pauseAll()
    }

    fun resumeAll() {
        DownloadManager.getInstance().startAll()
    }

    fun pauseItem(item: MutableLiveData<MediaItem>) {
        DownloadManager.getInstance().pauseItem(item)
    }

    fun resumeItem(item: MutableLiveData<MediaItem>) {
        DownloadManager.getInstance().resumeItem(item)
    }

    fun createItem(name: String, url: String) {
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(MainApplication.instance, "请输入下载链接地址", Toast.LENGTH_LONG).show()
            return
        }
        val success = DownloadManager.getInstance().createNew(url, name)
        if (success) {
            adapter.value!!.notifyDataSetChanged()
        } else {
            Toast.makeText(MainApplication.instance, "此下载任务已存在", Toast.LENGTH_LONG).show()
        }
    }

    fun deleteItem(item: MutableLiveData<MediaItem>) {
        DownloadManager.getInstance().deleteItem(item)
        adapter.value!!.notifyDataSetChanged()
    }

    fun hasItems() = DownloadManager.getInstance().hasItems()
    fun reDownload(item: MutableLiveData<MediaItem>) {
        DownloadManager.getInstance().reDownloadItem(item)
        adapter.value!!.notifyDataSetChanged()
    }
}