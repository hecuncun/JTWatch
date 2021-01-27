package com.lhzw.bluetooth.bean

import org.litepal.crud.LitePalSupport

/**
 *
@author：created by xtqb
@description: 运动数据
@date : 2019/11/18 11:11
 *
 */

data class SportInfoBean(
        var type: Int,
        var ymt: String,
        var time: String,
        var duration: String
) : LitePalSupport() {
    val id:Long=1  //uniquen
}