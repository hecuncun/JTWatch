package com.lhzw.bluetooth.bean

import org.litepal.crud.LitePalSupport

/**
 *
@author：created by xtqb
@description: 活动号表
@date : 2020/1/14 9:01
 *
 */

data class SportActivityBean(
        /*    响应码    */
        val response: String = 0x0D.toInt().toString(),
        /*    日期标识    */
        val daily_date: String,
        /*    日期时间    初始为日常日期 + 0x00, 0x00, 0x00,  之后从动态数据中获取*/
        var request_date: Long,
        /*    当前活动号对应的活动数量    */
        var current_activity_num: Int,
        /*      当前的活动号       */
        var current_activity_mark: Int,
        /*    活动起始地址    */
        var activities_addr: Int
) : LitePalSupport() {
    val id: Long = 0
}