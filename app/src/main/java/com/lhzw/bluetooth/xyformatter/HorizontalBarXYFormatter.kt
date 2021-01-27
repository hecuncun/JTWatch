package com.lhzw.bluetooth.xyformatter

import android.util.Log
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/25 10:43
 *
 */
class HorizontalBarXYFormatter(private var drawY: Boolean, private var lables : Array<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        if(drawY) {

            Log.e("Tag", "y : ${value}")
            return "${value.toInt()}%"
        } else {
            Log.e("Tag", "x : ${value}")
            return lables[(value/10).toInt()]
        }
        return super.getFormattedValue(value)
    }
}