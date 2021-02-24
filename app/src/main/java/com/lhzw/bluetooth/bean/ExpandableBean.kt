package com.lhzw.bluetooth.bean

/**
 * Created by heCunCun on 2021/2/23
 */
data class ExpandableBean(
        val isDef:Boolean,
        val groupName: String,
        val groupId: String,
        val childListBean: MutableList<GroupChildBean>
)

data class GroupChildBean(
        val showTitle:Boolean,
        val name: String,
        val id: String,
        val lat: Double,
        val lng: Double,
        val time: String
)