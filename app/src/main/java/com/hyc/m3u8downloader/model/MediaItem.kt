package com.hyc.m3u8downloader.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.hyc.m3u8downloader.utils.ItemDelegate
import com.hyc.m3u8downloader.utils.Sp
import org.greenrobot.eventbus.EventBus

@Entity
class MediaItem {
    @PrimaryKey()
    var id: Long? = null
    var name: String? = null//用户输入名称
    var url: String? = null//用户输入地址
    var state: Int = 0//0初始化等待，1正在下载，2下载暂停，3已下载，4，已下载，但没有找到mp4路径，5下载完成正在合并文件
    var mp4Path: String? = null//下载完成后的mp4路径
    var fileCount: Int? = null
    var downloadedCount: Int=0
    var parentPath: String? = null
    @Ignore
    var tsUrls: List<String>? = null
    @Ignore
    var index: Int = 0//当前显示的位置
    @Ignore
    var list:List<TSItem>?=null
    override fun toString(): String {
        return "MediaItem(id=$id, name=$name, url=$url, state=$state, mp4Path=$mp4Path)"
    }
    fun notifyChanged(){
        EventBus.getDefault().post(ItemChangeEvent(index))
        MyDatabase.getInstance().getMediaItemDao().updateMedia(this)
    }
//    @Relation(parentColumn = "id", entityColumn = "media_id")
//    var tsFiles: ArrayList<TSItem>? = null

}