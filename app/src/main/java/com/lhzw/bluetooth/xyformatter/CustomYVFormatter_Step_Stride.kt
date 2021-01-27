package com.lhzw.bluetooth.xyformatter

import com.github.mikephil.charting.formatter.ValueFormatter

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/25 9:31
 *
 */
// 步幅
class CustomYVFormatter_Step_Stride(private var drawY: Boolean) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        if (drawY) {
            return value.toString() + "米"
        } else {
            return value.toString() + "分"
        }
        return super.getFormattedValue(value)
    }
}