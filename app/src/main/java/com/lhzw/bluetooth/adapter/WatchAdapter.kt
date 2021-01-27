package com.lhzw.bluetooth.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.bean.ConnectWatchBean
import kotlinx.android.synthetic.main.item_sw_watch_info.view.*

/**
 * Date： 2020/6/17 0017
 * Time： 8:52
 * Created by xtqb.
 */

class WatchAdapter(val mContext: Context, var list: MutableList<ConnectWatchBean>?) : RecyclerView.Adapter<WatchAdapter.Viewholder>(){
    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name: TextView = itemView.tv_name
        val tv_watch_type: TextView = itemView.tv_watch_type
        val tv_watch_note: TextView = itemView.tv_watch_note
        val tv_watch_func: TextView = itemView.tv_watch_func
        val iv_iv_watch: ImageView = itemView.iv_watch
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): Viewholder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_sw_watch_info, null)
        return Viewholder(view)
    }

    override fun getItemCount(): Int {
        if (list == null || list?.size == 0) return 0
        return list!!.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        list?.let {
            holder.tv_name.text = it.get(position).name
            holder.tv_watch_type.text = it.get(position).type
            holder.tv_watch_note.text = it.get(position).content
            holder.tv_watch_func.text = it.get(position).func
        }
        holder.itemView.setTag(position)
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    private var onItemClickListener:OnItemClickListener?=null

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        onItemClickListener=listener
    }
}