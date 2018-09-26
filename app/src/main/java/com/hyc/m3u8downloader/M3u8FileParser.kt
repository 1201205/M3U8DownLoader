package com.hyc.m3u8downloader

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.hyc.m3u8downloader.model.TSItem
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.ArrayList

class M3u8FileParser {
    fun parse(id: Long, url: String, file: File, callBack: ParseCallBack) {
        val uri = Uri.parse(url)
        //目前默认使用http  因为https 很慢
        val host = "${uri.scheme}://${uri.host}"
        val more = (url.replace(uri.lastPathSegment, ""))
        val content = ((more.replace(uri.scheme, "")).replace("://", "")).replace(uri.host, "")
        Log.d("hyc-parse", "content--$content")
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
            val redirectUrl = if (nextLine.startsWith(content)) {
                host + nextLine
            } else {
                more + nextLine
            }

//            val strings = nextLine.split("/")
//            val title = ""
//            for (item in strings) {
//                if (!item.contains(".m3u8")) {
//                    title.plus(item)
//                    title.plus("/")
//                }
//            }
//            if (TextUtils.isEmpty(title) || more) {
//            }
            Log.d("hyc-parse", "need redownload url--$redirectUrl")
            callBack.onNeedDownLoad(redirectUrl)
            return
        }

        if (line.startsWith("#EXT-X-VERSION") || line.startsWith("#EXT-X-TARGETDURATION") || line.startsWith("#EXT-X-MEDIA-SEQUENCE")) {
            var hasUrl = false
            var list = ArrayList<TSItem>()
            var index = 0;
            while ((br.readLine().apply { line = this }) != null) {
                Log.e("hyc-parse", line)
                if (line!!.startsWith("#EXTINF")) {
                    hasUrl = true
                } else if (TextUtils.equals(line, "#EXT-X-ENDLIST")) {
                    break
                } else {
                    if (hasUrl) {
                        if (line!!.startsWith(content)) {
                            list.add(TSItem(index, host + line, id))
                            Log.d("hyc-parse", "add download url--(${host + line})")
                        } else {
                            list.add(TSItem(index, more + line, id))
                            Log.d("hyc-parse", "add download url--(${more + line})")
                        }
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