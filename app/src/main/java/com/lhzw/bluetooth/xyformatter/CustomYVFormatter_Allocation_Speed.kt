package com.lhzw.bluetooth.xyformatter

import android.util.Log
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/25 9:28
 *
 */
// 配速
class CustomYVFormatter_Allocation_Speed(private var drawY: Boolean, private var max: Float) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        if (drawY) {
            return valueOfY_format(value)
        } else {
            if (value <= 1 && value > 0) {
                return "${String.format("%.2f", value)}"
            } else if (value > 1) {
                return "${String.format("%.1f", value)}"
            } else {
                return "0.0"
            }
            return value.toString()
        }
        return super.getFormattedValue(value)
    }

    private fun valueOfY_format(value: Float): String {
        Log.e("H_AllocationY", "mValue : ${value}  $max")
        if (value != 0.0f) {
            var y_value = Math.abs(max - value).toInt()
            val min = y_value / 60
            val second = y_value % 60
            var str = ""
            if (min < 0x0A) {
                str += "0${min}${"\'"}"
            } else {
                str += "$min${"\'"}"
            }
            if (second < 0x0A) {
                str += "0$second${"\""}"
            } else {
                str += "$second${"\""}"
            }
            Log.e("H_AllocationY", "mValue :-------- ${str}")
            return str
        }
        return ""
    }
}