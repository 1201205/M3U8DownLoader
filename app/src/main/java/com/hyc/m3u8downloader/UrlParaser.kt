package com.hyc.m3u8downloader

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.ArrayList

class UrlParaser(list: ArrayList<String>) : Thread() {
    val client :OkHttpClient= OkHttpClient()
    var mList:ArrayList<String> =list

    override fun run() {
        while (mList.size>0){
            val url=mList.removeAt(0)
            try {
                val request=Request.Builder().url(url).build()
                client.newCall(request).execute().body()?.let {
                    Log.e("hyc--pass","${it.contentLength()}-----$url-----+${it.contentType()}")
                }
            }catch (e:Exception){
                e.printStackTrace()

            }

        }
    }
}