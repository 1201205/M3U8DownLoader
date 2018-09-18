package com.hyc.m3u8downloader.view

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.hyc.m3u8downloader.R
import com.hyc.m3u8downloader.databinding.ItemMainBinding
import com.hyc.m3u8downloader.model.MediaItem
import java.util.ArrayList

class MainAdapter2(items: ArrayList<MutableLiveData<MediaItem>>, context: Context) : RecyclerView.Adapter<MainAdapter2.ViewHolder>() {
    private var mItems: ArrayList<MutableLiveData<MediaItem>> = items
    private var mContext = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        var binding = DataBindingUtil.inflate<ItemMainBinding>(inflater, R.layout.item_main, parent, false)
        return ViewHolder(binding.root)
    }

    override fun getItemCount() = mItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position].value
        var binding = DataBindingUtil.getBinding<ItemMainBinding>(holder.itemView)
        binding!!.item = item
        binding.executePendingBindings()
        mItems[position].removeObservers((mContext) as LifecycleOwner)
        mItems[position].observe((mContext) as LifecycleOwner, Observer {
            if (holder.adapterPosition == position) {
                val item = mItems[position].value
                binding!!.item = item
                binding.executePendingBindings()
            }
        })
    }

    fun addItem(item: MediaItem) {
        val size = mItems.size
        var ixx = MutableLiveData<MediaItem>()
        ixx.value = item
        mItems.add(ixx)
        notifyItemChanged(size)
    }
    fun addItem(item: MutableLiveData<MediaItem>) {
        val size = mItems.size
        mItems.add(item)
        notifyItemInserted(size)
    }

    fun change(index: Int) {
        var item = mItems[index]
        var value = item.value
        value!!.state = index * 10
        item.postValue(value)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        var tvName: TextView = view.findViewById(R.id.tv_name)
//        var tvPath: TextView = view.findViewById(R.id.tv_path)
//        var tvCount: TextView = view.findViewById(R.id.tv_count)
//        var ivPic: ImageView = view.findViewById(R.id.iv_pic)
//        var btState: Button = view.findViewById(R.id.bt_state)
    }
}