package com.lhzw.bluetooth.view

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Date： 2020/6/17 0017
 * Time： 10:53
 * Created by xtqb.
 */

class SpaceItemDecoration(val space: Int) : RecyclerView.ItemDecoration() {
    // 间距
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        outRect.bottom = space
        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildPosition(view) == 0)
            outRect.top = space / 2
    }
}