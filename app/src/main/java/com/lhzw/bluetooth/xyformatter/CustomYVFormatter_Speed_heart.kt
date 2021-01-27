package com.lhzw.bluetooth.xyformatter

import android.util.Log
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/25 9:29
 *
 */

// 心率
class CustomYVFormatter_Speed_heart(private var drawY: Boolean, var zoom : Int) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        if (drawY) {
            Log.e("Tag", "$value   $zoom")
            return "${(value.toInt() * zoom)}"
        } else {
            return value.toString()
        }
        return super.getFormattedValue(value)
    }
}