package com.hyc.m3u8downloader

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.ArrayList

class M3u8FileParser {
    fun parse(url: String, file: File, callBack: ParseCallBack) {
        val uri = Uri.parse(url)
        //目前默认使用http  因为https 很慢
        val host =  "http://${uri.host}"
        val more = (url.replace(uri.lastPathSegment, "")).replace("https://","http://")
        val reader = InputStreamReader(FileInputStream(file))
        val br = BufferedReader(reader)
        var line: String? = ""
        line = br.readLine()
        if (!TextUtils.equals("#EXTM3U", line)) {
            callBack.onParseFailed("the input file is not a correct m3u8 file")
            return
        }
        line = br.readLine()
        if (line.startsWith("#EXT-X-STREAM-INF")) {
            //再次下载文件并解析
            val redirectUrl = host + br.readLine()
            Log.d("hyc-parse", "need redownload url--$redirectUrl")
            callBack.onNeedDownLoad(redirectUrl)
            return
        }

        if (line.startsWith("#EXT-X-VERSION") || line.startsWith("#EXT-X-TARGETDURATION") || line.startsWith("#EXT-X-MEDIA-SEQUENCE")) {
            var hasUrl = false
            var list = ArrayList<String>()
            while ((br.readLine().apply { line = this }) != null) {
                Log.e("hyc-parse", line)
                if (line!!.startsWith("#EXTINF")) {
                    hasUrl = true
                } else if (TextUtils.equals(line, "#EXT-X-ENDLIST")) {
                    break
                } else {
                    if (hasUrl) {
                        if (line!!.split("/").size > 1) {
                            list.add(host + line)
                            Log.d("hyc-parse", "add download url--(${host + line})")
                        } else {
                            list.add(more + line)
                            Log.d("hyc-parse", "add download url--(${more + line})")
                        }
                        hasUrl = false
                    }
                }
            }

            callBack.onParseSuccess(list)
        } else {
            callBack.onParseFailed("the input file is not a correct m3u8 file")
            return
        }


    }
}