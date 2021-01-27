package com.lhzw.bluetooth.uitls.douglas.bean

import android.support.annotation.NonNull
import com.amap.api.maps.model.LatLng


/**
 *
@author：created by xtqb
@description:
@date : 2020/5/15 9:58
 *
 */
class LatLngPoint : Comparable<LatLngPoint> {
    /**
     * 用于记录每一个点的序号
     */
    var id = 0

    /**
     * 每一个点的经纬度
     */
    var latLng: LatLng? = null

    constructor(id: Int, latLng: LatLng?) {
        this.id = id
        this.latLng = latLng
    }

    override fun compareTo(@NonNull o: LatLngPoint): Int {
        if (id < o.id) {
            return -1
        } else if (id > o.id) return 1
        return 0
    }
}