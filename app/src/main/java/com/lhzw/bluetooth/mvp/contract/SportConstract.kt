package com.lhzw.bluetooth.mvp.contract

import android.app.Activity
import android.graphics.Bitmap
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.model.Marker
import com.lhzw.bluetooth.base.BaseIView
import com.lhzw.bluetooth.bean.SportDetailInfobean
import org.litepal.crud.LitePalSupport

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/18 15:27
 *
 */
interface SportConstract {
    interface Model {
        fun detachView()
        fun initMap(mMapView: MapView?): AMap?
        fun checkPermissions(activity: Activity, permissions: Array<String>): Boolean
        fun initChart(activity: Activity, convertView: android.view.View)
        fun queryData(mark: String, type: Int): List<SportDetailInfobean>?
        fun <T : LitePalSupport> queryData(mark: String): List<T>?
        fun getDistanceMap(): MutableMap<Int, Int>?
        fun initFont(activity: Activity, convertView: android.view.View)
    }

    interface View : BaseIView {
        fun cancel()
        fun toastMsg(msg: String)
    }

    interface Presenter {
        fun getSha1(): String?
        fun initMap(activity: Activity, mMapView: MapView?): AMap?
        fun requirePermission(activity: Activity): Boolean
        fun initChart(activity: Activity, convertView: android.view.View)
        fun showSharePopuWindow(activity: Activity, stareBitmap: Bitmap?)
        fun initView(activity: Activity, convertView: android.view.View)
        fun getCurrentMarker(): Marker?
        fun startShareActivity(activity: Activity?, path: String?)
    }
}