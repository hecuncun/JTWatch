package com.lhzw.bluetooth.bean

import org.litepal.crud.LitePalSupport

/**
 * Created by heCunCun on 2021/2/19
 */
data class GuardLocationBean(
        val gid: String,//守护id
        val lat: Double,//纬度
        val lon: Double,//经度
        val time: Long//定位时间
) : LitePalSupport() {
    val id: Long = 1
}