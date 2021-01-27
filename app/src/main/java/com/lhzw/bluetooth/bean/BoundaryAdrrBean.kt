package com.lhzw.bluetooth.bean

import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.uitls.BaseUtils
import org.litepal.crud.LitePalSupport

/**
 *
@author：created by xtqb
@description:
@date : 2020/1/13 15:22
 *
 */
data class BoundaryAdrrBean(
        /*    响应码    */
        val response: String = 0x0E.toString(),
        /*   计步开始地址   */
        val record_steps_start: Int,
        /*   计步结束地址   */
        val record_steps_end: Int,
        /*   心率开始地址   */
        val heart_rate_start: Int,
        /*   心率结束地址   */
        val heart_rate_end: Int,
        /*   气压开始地址   */
        val air_pressure_start: Int,
        /*   气压结束地址   */
        val air_pressure_end: Int,
        /*   GPS开始地址   */
        val gps_start: Int,
        /*   GPS结束地址   */
        val gps_end: Int,
        /*   距离开始地址   */
        val distance_start: Int,
        /*   距离结束地址   */
        val distance_end: Int,
        /*   卡路里开始地址   */
        val calorie_start: Int,
        /*   卡路里结束地址   */
        val calorie_end: Int,
        /*   速度开始地址   */
        val speed_start: Int,
        /*   速度结束地址   */
        val speed_end: Int,
        /*   距离标点开始地址   */
        val distance_point_start: Int,
        /*   距离标点结束地址   */
        val distance_point_end: Int
) : LitePalSupport() {
    val id: Long = 0

    companion object {
        fun parserBoundaryAdrr(content: ByteArray?) {
            content?.let {
                val list = content.toList()
                val record_steps_start = BaseUtils.byteToInt(list.subList(1, 5))
                val record_steps_end = BaseUtils.byteToInt(list.subList(1 + 4, 5 + 4))
                val heart_rate_start = BaseUtils.byteToInt(list.subList(1 + 4 * 2, 5 + 4 * 2))
                val heart_rate_end = BaseUtils.byteToInt(list.subList(1 + 4 * 3, 5 + 4 * 3))
                val air_pressure_start = BaseUtils.byteToInt(list.subList(1 + 4 * 4, 5 + 4 * 4))
                val air_pressure_end = BaseUtils.byteToInt(list.subList(1 + 4 * 5, 5 + 4 * 5))
                val gps_start = BaseUtils.byteToInt(list.subList(1 + 4 * 6, 5 + 4 * 6))
                val gps_end = BaseUtils.byteToInt(list.subList(1 + 4 * 7, 5 + 4 * 7))
                val distance_start = BaseUtils.byteToInt(list.subList(1 + 4 * 8, 5 + 4 * 8))
                val distance_end = BaseUtils.byteToInt(list.subList(1 + 4 * 9, 5 + 4 * 9))
                val calorie_start = BaseUtils.byteToInt(list.subList(1 + 4 * 10, 5 + 4 * 10))
                val calorie_end = BaseUtils.byteToInt(list.subList(1 + 4 * 11, 5 + 4 * 11))
                val speed_start = BaseUtils.byteToInt(list.subList(1 + 4 * 12, 5 + 4 * 12))
                val speed_end = BaseUtils.byteToInt(list.subList(1 + 4 * 13, 5 + 4 * 13))
                val distance_point_start = BaseUtils.byteToInt(list.subList(1 + 4 * 14, 5 + 4 * 14))
                val distance_point_end = BaseUtils.byteToInt(list.subList(1 + 4 * 15, 5 + 4 * 15))
                val bean = BoundaryAdrrBean(
                        0x0E.toString(),
                        record_steps_start,
                        record_steps_end,
                        heart_rate_start,
                        heart_rate_end,
                        air_pressure_start,
                        air_pressure_end,
                        gps_start,
                        gps_end,
                        distance_start,
                        distance_end,
                        calorie_start,
                        calorie_end,
                        speed_start,
                        speed_end,
                        distance_point_start,
                        distance_point_end
                )
                CommOperation.insert(bean)
            }
        }
    }
}
