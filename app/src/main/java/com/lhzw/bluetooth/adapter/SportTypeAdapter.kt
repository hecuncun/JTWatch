package com.lhzw.bluetooth.adapter

import android.app.Activity
import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.lhzw.bluetooth.R


/**
 *
@author：created by xtqb
@description:
@date : 2020/4/24 11:18
 *
 */

class SportTypeAdapter(val activity: Activity, val list: Array<String>, val listener: OnItemClickListener?, val height: Int) : RecyclerView.Adapter<SportTypeAdapter.Viewholder>(), View.OnClickListener {
    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView as TextView
    }

    override fun onCreateViewHolder(group: ViewGroup, position: Int): Viewholder {
        var textview = TextView(group.context)
        textview.gravity = Gravity.CENTER
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        textview.setBackgroundColor(group.resources.getColor(R.color.black))
        val states = arrayOf(intArrayOf(-android.R.attr.state_pressed), intArrayOf(android.R.attr.state_pressed)) //把两种状态一次性添加
        val colors = intArrayOf(
                activity.resources.getColor(R.color.gray),
                activity.resources.getColor(R.color.white)
        ) //把两种颜色一次性添加
        val colorStateList = ColorStateList(states, colors)
        textview.setTextColor(colorStateList)
        textview.textSize = 16f
        var display = activity.windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        params.width = metrics.widthPixels / 3
        params.height = height
        params.gravity = Gravity.CENTER
        textview.layoutParams = params
        val holder = Viewholder(textview)
        return holder
    }

    override fun getItemCount(): Int {
        if (list == null) return 0
        return list.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.textView.text = list[position]
        listener?.let {
            holder.textView.tag = position
            holder.textView.setOnClickListener(this@SportTypeAdapter)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(pos: Int)
    }

    override fun onClick(v: View?) {
        v?.let {
            val pos = v.tag as Int
            listener?.onItemClick(pos)
        }
    }


}