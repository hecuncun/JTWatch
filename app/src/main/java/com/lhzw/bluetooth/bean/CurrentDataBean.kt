package com.lhzw.bluetooth.bean

import com.lhzw.bluetooth.uitls.BaseUtils
import org.litepal.crud.LitePalSupport

/**
 * Created by heCunCun on 2020/1/9
 */
data class CurrentDataBean(
        val response: String?,
        val dailyStepNumTotal: Int,
        val dailyCalTotal: Int,//单位是cal
        val dailyMileageTotal: Int,//单位是CM
        val sportStepNumTotal: Int,
        val sportCalTotal: Int,//单位是cal
        val sportMileageTotal: Int//单位是CM
) : LitePalSupport() {
    val id: Long = 1

    companion object {
        fun createBean(content: ByteArray?): CurrentDataBean? {
            content?.let {
                val bytes = content.toList()
                val dailyStepNumTotal = BaseUtils.byteToInt(bytes.subList(1, 5))
                val dailyCalTotal = BaseUtils.byteToInt(bytes.subList(5, 9))
                val dailyMileageTotal = BaseUtils.byteToInt(bytes.subList(9, 13))
                val sportStepNumTotal = BaseUtils.byteToInt(bytes.subList(13, 17))
                val sportCalTotal = BaseUtils.byteToInt(bytes.subList(17, 21))
                val sportMileageTotal = BaseUtils.byteToInt(bytes.subList(21, 25))
                val bean = CurrentDataBean(bytes[0].toString(), dailyStepNumTotal, dailyCalTotal, dailyMileageTotal, sportStepNumTotal, sportCalTotal, sportMileageTotal)
                return bean
            }

            return null
        }
    }
}