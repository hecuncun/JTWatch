package com.lhzw.bluetooth.bean

import android.content.ContentValues
import android.util.Log
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.uitls.BaseUtils
import org.litepal.LitePal
import org.litepal.crud.LitePalSupport

/**
 *
@author：created by xtqb
@description: 日常数据
@date : 2020/1/10 8:53
 *
 */

data class DailyDataBean(
        /*    响应码    */
        val response: String = 0x0C.toInt().toString(),
        /*    运动天数    */
        val sport_days: Int,
        /*    运动日期    */
        val sport_date: String,
        /*    运动数量    */
        val sport_num: Int,
        /*    起始地址    */
        val start_addr: Int,
        /*     数据长度   */
        val data_len: Int
) : LitePalSupport() {
    val id: Long = 0

    companion object {
        // 存储日常数据
        fun parserDailyData(content: ByteArray?, isSyncAscending: Boolean, body: (datas: MutableList<DailyDataBean>) -> Unit) {
            content?.let {
                val response = content[0].toInt().toString()
                val sport_days = content[1].toInt()
                val datas = ArrayList<DailyDataBean>()
                for (day in 0 until sport_days) {
                    var builder = StringBuilder()
                    builder.append(content[2 + 0 + 12 * day])
                    builder.append("-")
                    builder.append(content[2 + 1 + 12 * day])
                    builder.append("-")
                    builder.append(content[2 + 2 + 12 * day])
                    val sport_date = builder.toString()
                    val sport_num = content[2 + 3 + 12 * day].toInt()
                    val start_addr = BaseUtils.byteToInt(listOf(
                            content[2 + 4 + 12 * day],
                            content[2 + 5 + 12 * day],
                            content[2 + 6 + 12 * day],
                            content[2 + 7 + 12 * day]
                    ))
                    val data_len = BaseUtils.byteToInt(listOf(
                            content[2 + 8 + 12 * day],
                            content[2 + 9 + 12 * day],
                            content[2 + 10 + 12 * day],
                            content[2 + 11 + 12 * day]
                    ))

                    val list = CommOperation.query(DailyDataBean::class.java, "sport_date", sport_date)
                    list?.forEach {
                        LitePal.delete(DailyDataBean::class.java, it.id)
                    }
                    // 保存数据
                    val bean = DailyDataBean(
                            response,
                            sport_days,
                            sport_date,
                            sport_num,
                            start_addr,
                            data_len
                    )
                    CommOperation.insert(bean)
                    if (isSyncAscending) {
                        if (sport_date.equals(BaseUtils.getCurrentData())) datas.add(bean)
                    } else {
                        datas.add(bean)
                    }
                    // 活动表数据更新 判断该活动号是否存在 判断活动数量是否大于0
                    val activityBeans = CommOperation.query(SportActivityBean::class.java, "daily_date", sport_date)
                    if (activityBeans.isNotEmpty()) {
//                        CommOperation.delete(SportActivityBean::class.java, "daily_date", sport_date)
                        val value = ContentValues();
                        value.put("current_activity_num", sport_num)
                        value.put("activities_addr", start_addr)
                        Log.e("parserDaily", "$sport_date   sport_num  =  ${list[0].sport_num}  new_sport_num = ${sport_num}")
                        CommOperation.update(SportActivityBean::class.java, value, activityBeans[0].id)
                    } else {
                        val response = 0x0D.toInt().toString()
                        val dateBytes = listOf<Byte>(
                                content[2 + 0 + 12 * day], content[2 + 1 + 12 * day], content[2 + 2 + 12 * day],
                                0x00, 0x00, 0x00)
                        val sportActivityBean = SportActivityBean(
                                response,
                                sport_date,
                                BaseUtils.byteToLong(dateBytes),
                                sport_num,
                                0,
                                start_addr)
                        CommOperation.insert(sportActivityBean)
                    }
                }
                body(datas)
            }
        }
    }
}