package com.hyc.m3u8downloader

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.hyc.m3u8downloader.model.TSItem
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.ArrayList

class M3u8FileParser {
    fun parse(id: Long, url: String, file: File, callBack: ParseCallBack) {
        Log.d("hyc-parse", "input url--$url")
        val uri = Uri.parse(url)
        val host = "${uri.scheme}://${uri.host}"
        val more = (url.replace(uri.lastPathSegment, ""))
        val content = ((more.replace(uri.scheme, "")).replace("://", "")).replace(uri.host, "")
//        Log.d("hyc-parse", "content--$content")
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
            val nextLine = br.readLine()
            val redirectUrl = URL(URL(url), nextLine).toString()
            callBack.onNeedDownLoad(redirectUrl)
            return
        }

        if (line.startsWith("#EXT-X-VERSION") || line.startsWith("#EXT-X-TARGETDURATION") || line.startsWith("#EXT-X-MEDIA-SEQUENCE")) {
            var hasUrl = false
            var list = ArrayList<TSItem>()
            var index = 0;
            while ((br.readLine().apply { line = this }) != null) {
//                Log.e("hyc-parse", line)
                if (line!!.startsWith("#EXTINF")) {
                    hasUrl = true
                } else if (TextUtils.equals(line, "#EXT-X-ENDLIST")) {
                    break
                } else {
                    if (hasUrl) {
                        list.add(TSItem(index, URL(URL(url), line).toString(), id))
//                        Log.d("hyc-parse", "add download url--(${URL(URL(url), line).toString()})")
                        hasUrl = false
                        index++
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