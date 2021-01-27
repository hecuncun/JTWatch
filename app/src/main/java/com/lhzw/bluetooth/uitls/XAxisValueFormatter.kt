package com.lhzw.bluetooth.uitls

import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * Created by heCunCun on 2019/11/21
 */
class XAxisValueFormatter(private val mLabels: Array<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String? {
        return if(mLabels.size > value.toInt()){
            mLabels[value.toInt()]
        }else{
            null
        }

    }
}