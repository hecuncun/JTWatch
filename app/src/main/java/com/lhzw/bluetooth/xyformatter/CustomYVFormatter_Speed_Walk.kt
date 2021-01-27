package com.lhzw.bluetooth.xyformatter

import com.github.mikephil.charting.formatter.ValueFormatter

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/25 9:30
 *
 */
// 步频
class CustomYVFormatter_Speed_Walk(private var drawY: Boolean, var zoom: Int) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        if (drawY) {
            return "${value.toInt() * zoom}"
        } else {
            return "${String.format("%.1f", value)}"
        }
    }
}