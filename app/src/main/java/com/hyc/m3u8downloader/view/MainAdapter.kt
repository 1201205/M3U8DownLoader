package com.hyc.m3u8downloader.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.hyc.m3u8downloader.R
import com.hyc.m3u8downloader.model.MediaItem
import java.util.ArrayList

class MainAdapter(items: ArrayList<MediaItem>) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    private var mItems: ArrayList<MediaItem> = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_main, parent, false))
    }

    override fun getItemCount() = mItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position]
        item.index = position
        holder.tvName.text = "当前状态："+item.state
        holder.tvPath.text = item.parentPath
        holder.tvCount.text="下载数量"+item.downloadedCount
    }

    fun addItem(item: MediaItem) {
        val size = mItems.size
        mItems.add(item)
        notifyItemChanged(size)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvName: TextView = view.findViewById(R.id.tv_name)
        var tvPath: TextView = view.findViewById(R.id.tv_path)
        var tvCount: TextView = view.findViewById(R.id.tv_count)
        var ivPic: ImageView = view.findViewById(R.id.iv_pic)
        var btState: Button = view.findViewById(R.id.bt_state)
    }
}