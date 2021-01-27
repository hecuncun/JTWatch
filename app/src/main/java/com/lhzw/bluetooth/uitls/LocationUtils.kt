package com.lhzw.bluetooth.uitls

import android.animation.ArgbEvaluator
import android.graphics.Point
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import android.widget.TextView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.Projection
import com.amap.api.maps.model.*
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.application.App.Companion.context
import com.lhzw.bluetooth.constants.Constants


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/26 11:21
 *
 */

class LocationUtils : AMapLocationListener {
    private var markers: ArrayList<Marker>? = ArrayList()
    private var paths: MutableList<Polyline>? = ArrayList()
    private var callBack: ILocationCallBack? = null
    private var center_lat by Preference(Constants.CENTER_LAT, Constants.LAT)
    private var center_lgt by Preference(Constants.CENTER_LGT, Constants.LGT)
    private var mGradientHelper: GradientHelper = GradientHelper(Constants.startColor, Constants.endColor)

    fun startLocate() {
        var aMapLocationClient = AMapLocationClient(context);
        aMapLocationClient.apply {
            //设置监听回调
            setLocationListener(this@LocationUtils);
            //初始化定位参数
            var clientOption = AMapLocationClientOption();
            clientOption.apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Battery_Saving;
                isNeedAddress = true;
                isOnceLocation = false;
                //设置是否强制刷新WIFI，默认为强制刷新
                isWifiActiveScan = true;
                //设置是否允许模拟位置,默认为false，不允许模拟位置
                isMockEnable = false;
                //设置定位间隔
                interval = 2000;
                aMapLocationClient?.setLocationOption(clientOption);
            }
            startLocation();
        }
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation != null) {
            if (aMapLocation.errorCode == 0) {
                //定位成功完成回调
                aMapLocation?.apply {
                    var country = aMapLocation.country
                    var province = aMapLocation.province
                    var city = aMapLocation.city
                    var district = aMapLocation.district
                    var street = aMapLocation.street
                    var lat = aMapLocation.latitude
                    var lgt = aMapLocation.longitude
                    val distance = AMapUtils.calculateLineDistance(LatLng(lat, lgt), LatLng(center_lat, center_lgt))
                    if (distance > 5) {
                        center_lat = lat
                        center_lgt = lgt
                    }
                    callBack?.callBack(country + province + city + district + street, lat, lgt, aMapLocation)
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.errorCode + ", errInfo:"
                        + aMapLocation.errorInfo)
            }
        }
    }

    /**
     * 自定义起始位置图标
     *
     * @return
     */
    private fun getMarkerOption(str: String, latLng: LatLng, icon_id: Int): MarkerOptions {
        var markerOptions = MarkerOptions()
        markerOptions.icon(BitmapDescriptorFactory.fromResource(icon_id))
        markerOptions.position(latLng)
//        markerOptions.title(str)
//        markerOptions.snippet("纬度:${latLng.latitude}   经度:${latLng.longitude}")
//        markerOptions.period(100)
        return markerOptions
    }

    /**
     * 自定义 marker view options
     */
    private fun getMarkerOption(latLng: LatLng, distance: String): MarkerOptions {
        val converview = LayoutInflater.from(context).inflate(R.layout.marker_view, null)
        converview.findViewById<TextView>(R.id.tv_distance).text = distance
        var markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.draggable(false)
        markerOptions.icon(BitmapDescriptorFactory.fromView(converview))
        return markerOptions
    }

    /**
     *
     * 绘制路径
     *
     */
    fun drawPath(aMap: AMap, list: MutableList<LatLng>, distanceMap: MutableMap<Int, Int>?) {
        markers?.let {
            it.clear()
        }
        paths?.forEach {
            it.remove()
        }

        if (list != null && list.size > 0) {
            val path_colors = arrayOf(R.color.red_path, R.color.yellow_path, R.color.green_path, R.color.cyan_path)
            markers?.add(aMap.addMarker(getMarkerOption("起点", list[0], R.mipmap.location_start)))
            var oldLatLng: LatLng = list[0]
            var polyLineList = ArrayList<PolylineOptions>()
            var distance = 0.0f
            var counter = 0
            var lastColorId = -1
            val points = ArrayList<LatLng>()
            val classify = classifyColor(distanceMap)
            var dis_total = 0.0
            var pathCounter = 0
            if (classify != null && classify.isNotEmpty()) {
                list.forEach { it ->
                    val tem = AMapUtils.calculateLineDistance(oldLatLng, it)
//                    val tem = BaseUtils.calculateDistance(oldLatLng, it)
                    distance += tem
                    dis_total += tem
                    if (distance > 1000) {
                        distance = 0.0f
                        points.add(it)
                        if (lastColorId == -1) {
                            lastColorId = classify[counter] + 1
                            polyLineList.add(calculateRoute(points, path_colors[classify[counter]], path_colors[classify[counter] + 1])!!)
                        } else {
                            if (counter >= classify.size) {
                                counter = 0
                            }
                            polyLineList.add(calculateRoute(points, path_colors[lastColorId], path_colors[classify[counter] + 1])!!)
                            lastColorId = classify[counter] + 1
                        }
                        points.clear()
                        points.add(it)
                        counter++
                        pathCounter++
                        markers?.add(aMap.addMarker(getMarkerOption(it, "$pathCounter")))
                    } else {
                        points.add(it)
                    }
                    oldLatLng = it;
                }
                Log.e("Distance", "total ---------------   $dis_total")
                if (points.size > 0) {
                    if (lastColorId == -1) {
                        polyLineList.add(calculateRoute(points, path_colors[0], path_colors[path_colors.size - 1])!!)
                    } else {
                        polyLineList.add(calculateRoute(points, path_colors[lastColorId], path_colors[path_colors.size - 1])!!)
                    }
                }
            } else {
                polyLineList.add(calculateRoute(list, path_colors[0], path_colors[path_colors.size - 1])!!)
            }
            polyLineList.forEach {
                paths?.add(aMap.addPolyline(it))
            }
            markers?.add(aMap.addMarker(getMarkerOption("终点", list.get(list.size - 1), R.mipmap.location_stop)))
            polyLineList.clear()
            list.clear()
        }
    }

    interface ILocationCallBack {
        fun callBack(str: String, lat: Double, lgt: Double, aMapLocation: AMapLocation);
    }

    fun detachCallBack() {
        paths?.apply {
            forEach {
                it.remove()
            }
            clear()
            paths = null
        }
        markers?.let {
            it.clear()
        }
        markers = null
        callBack = null
    }

    fun setLocationCallBack(callBack: ILocationCallBack) {
        this@LocationUtils.callBack = callBack
    }

    /**
     * marker点击时跳动一下
     */
    fun jumpPoint(marker: Marker, proj: Projection) {
        val handler = Handler()
        val start = SystemClock.uptimeMillis() // 从开机到现在的毫秒数（手机睡眠时间不包括）
        val markerLatlng = marker.position
        val markerPoint: Point = proj.toScreenLocation(markerLatlng)
        markerPoint.offset(0, -100)
        val startLatLng = proj.fromScreenLocation(markerPoint)
        val duration: Long = 1500
        val interpolator: Interpolator = BounceInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t: Float = interpolator.getInterpolation(elapsed.toFloat()
                        / duration)
                val lng = t * markerLatlng.longitude + (1 - t) * startLatLng.longitude
                val lat = t * markerLatlng.latitude + (1 - t) * startLatLng.latitude
                marker.position = LatLng(lat, lng)
                if (t < 1.0) {
                    handler.postDelayed(this, 16)
                } else {
                    marker.position = markerLatlng
                }
            }
        })
    }

    private fun calculateRoute(points: MutableList<LatLng>, startColor: Int, endColor: Int): PolylineOptions? {
        if (points.size > 0) {
            val argbEvaluator = ArgbEvaluator() //渐变色计算类
            val colorStart: Int = App.instance.resources.getColor(startColor)
            val colorEnd: Int = App.instance.resources.getColor(endColor)
            val pathPointList: ArrayList<LatLng> = ArrayList()
            val colorList: MutableList<Int> = ArrayList()
            val size: Int = points.size //路径上所有的点
            for (i in 0 until size) {
                val latLng = points.get(i)
                val currentColor = argbEvaluator.evaluate(i.toFloat() / size.toFloat(), colorStart, colorEnd) as Int //计算每个点需要的颜色值
                colorList.add(currentColor)
                pathPointList.add(latLng)
            }
            return PolylineOptions()
                    .width(BaseUtils.dip2px(5).toFloat()) //                                .color(Color.parseColor("#FF5934"))
                    .colorValues(colorList)
                    .useGradient(true)
                    .addAll(pathPointList)
        }
        return null
    }

    private fun classifyColor(distanceMap: MutableMap<Int, Int>?): IntArray {
        var max = 0.0f
        var min = Float.MAX_VALUE
        val size = distanceMap?.size
        val arry: IntArray = IntArray(size!!)
        distanceMap?.forEach { (key, value) ->
            if (value > max) {
                max = value.toFloat()
            }
            if (min > value) {
                min = value.toFloat()
            }
        }
        val space = (max - min) / 3
        for (index in 1..size) {
            var pos = 0
            var isOver = true
            while (isOver) {
                if (distanceMap!![index]!! >= min + space * pos && distanceMap!![index]!! < min + space * (pos + 1) + 1) {
                    isOver = false
                    arry[index - 1] = pos
                } else {
                    pos++
                }
            }
        }
        distanceMap.forEach { (t, u) ->
            Log.e("COLORS", "value   t   " + t + "   u   " + u)
        }
        arry.forEach {
            Log.e("COLORS", "value   -----------   " + it)
        }
        return arry
    }
}
