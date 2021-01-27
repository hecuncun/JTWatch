package com.lhzw.bluetooth.bean

import android.util.Log
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.uitls.BaseUtils
import org.litepal.LitePal
import org.litepal.crud.LitePalSupport

/**
 *
@author：created by xtqb
@description: 日常详细数据
@date : 2020/1/10 10:44
 *
 */
data class DailyInfoDataBean(
        /*   日期   */
        val daily_date: String,
        /*   索引号   */
        val daily_index: Int,
        /*   一天中的第几个时段 1 - 24   */
        val hour: Int,
        /*   日常步数   */
        val daily_steps: Int,
        /*   日常卡路里   */
        val daily_calorie: Int,
        /*   日常距离   */
        val daily_distance: Int,
        /*   运动步数   */
        val sport_steps: Int,
        /*   运动卡路里   */
        val sport_calorie: Int,
        /*   运动距离   */
        val sport_distance: Int,
        /*   气压   */
        val air_pressure: Int,
        /*   海拔   */
        val elevation: Int,
        /*   温度   */
        val temperature: Int,
        /*   remark   */
        val remark: String = ""
) : LitePalSupport() {
    val id: Long = 0

    companion object {
        fun parserDailyInfoBean(map: Map<String, MutableList<Byte>>, body: () -> Unit) {
            val HOUR_MAX = 24
            map.filter {
                it.value.size == 836
            }.forEach { (key, value) ->
                // 删除 数据
                val list = CommOperation.query(DailyInfoDataBean::class.java, "daily_date", key)
                list?.forEach {
                    LitePal.delete(DailyInfoDataBean::class.java, it.id)
                }
//                Log.e("air_pressure", "result : ${BaseUtils.byte2HexStr(value.toByteArray())}")
                for (hour in 1..HOUR_MAX) {
                    val daily_date: String = key
                    val daily_index = value[3].toInt()
                    val daily_steps = BaseUtils.byteToInt(value.subList(hour * 4, (hour + 1) * 4))
                    val daily_calorie = BaseUtils.byteToInt(value.subList(hour * 4 + 1 * 4 * HOUR_MAX, (hour + 1) * 4 + 1 * 4 * HOUR_MAX))
                    val daily_distance = BaseUtils.byteToInt(value.subList(hour * 4 + 2 * 4 * HOUR_MAX, (hour + 1) * 4 + 2 * 4 * HOUR_MAX))
                    val sport_steps = BaseUtils.byteToInt(value.subList(hour * 4 + 3 * 4 * HOUR_MAX, (hour + 1) * 4 + 3 * 4 * HOUR_MAX))
                    val sport_calorie = BaseUtils.byteToInt(value.subList(hour * 4 + 4 * 4 * HOUR_MAX, (hour + 1) * 4 + 4 * 4 * HOUR_MAX))
                    val sport_distance = BaseUtils.byteToInt(value.subList(hour * 4 + 5 * 4 * HOUR_MAX, (hour + 1) * 4 + 5 * 4 * HOUR_MAX))
                    val air_pressure = BaseUtils.byteToInt(value.subList(hour * 4 + 6 * 4 * HOUR_MAX, (hour + 1) * 4 + 6 * 4 * HOUR_MAX))
                    val elevation = BaseUtils.byteToInt(value.subList(2 + hour * 2 + 7 * 4 * HOUR_MAX, 4 + hour * 2 + 7 * 4 * HOUR_MAX))
                    val temperature = BaseUtils.byteToInt(value.subList(2 + hour * 2 + 7 * 4 * HOUR_MAX + 2 * HOUR_MAX, 4 + hour * 2 + 7 * 4 * HOUR_MAX + 2 * HOUR_MAX))

                    // 保存 新数据
                    val bean = DailyInfoDataBean(
                            daily_date,
                            daily_index,
                            hour,
                            daily_steps,
                            daily_calorie,
                            daily_distance,
                            sport_steps,
                            sport_calorie,
                            sport_distance,
                            air_pressure,
                            elevation,
                            temperature
                    )
                    CommOperation.insert(bean)
                }
            }
            // 继续获手表数据
            body()
        }
    }
}