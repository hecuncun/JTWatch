package com.lhzw.bluetooth.uitls.douglas

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.lhzw.bluetooth.uitls.douglas.bean.LatLngPoint
import java.util.*
import kotlin.collections.ArrayList


/**
 *
@author：created by xtqb
@description:
@date : 2020/5/15 10:00
 *
 */
class Douglas {
    private var dMax = 0.0
    private var start = 0
    private var end = 0
    private val points: MutableList<LatLngPoint> = ArrayList()

    constructor(mLineInit: List<LatLng>, dmax: Double) {
        this.dMax = dmax
        this.start = 0
        end = mLineInit.size - 1
        for (i in mLineInit.indices) {
            points.add(LatLngPoint(i, mLineInit[i]))
        }
    }

    /**
     * 压缩经纬度点
     *
     * @return
     */
    fun compress(): List<LatLng?>? {
        val size: Int = points.size
        val endLatLngs = ArrayList<LatLngPoint>()
        val latLngPoints = compressLine(points, endLatLngs, start, end, dMax)
        latLngPoints.add(points.get(0))
        latLngPoints.add(points.get(size - 1))
        //对抽稀之后的点进行排序
        latLngPoints.sortWith(Comparator { o1, o2 -> o1!!.compareTo(o2!!) })
        val latLngs: MutableList<LatLng?> = ArrayList()
        for (point in latLngPoints) {
            latLngs.add(point.latLng)
        }
        return latLngs
    }


    /**
     * 根据最大距离限制，采用DP方法递归的对原始轨迹进行采样，得到压缩后的轨迹
     * x
     *
     * @param originalLatLngs 原始经纬度坐标点数组
     * @param endLatLngs      保持过滤后的点坐标数组
     * @param start           起始下标
     * @param end             结束下标
     * @param dMax            预先指定好的最大距离误差
     */
    private fun compressLine(originalLatLngs: MutableList<LatLngPoint>, endLatLngs: MutableList<LatLngPoint>, start: Int, end: Int, dMax: Double): MutableList<LatLngPoint> {
        if (start < end) {
            //递归进行调教筛选
            var maxDist = 0.0
            var currentIndex = 0
            for (i in start + 1 until end) {
                val currentDist = distToSegment(originalLatLngs[start], originalLatLngs[end], originalLatLngs[i])
                if (currentDist > maxDist) {
                    maxDist = currentDist
                    currentIndex = i
                }
            }
            //若当前最大距离大于最大距离误差
            if (maxDist >= dMax) {
                //将当前点加入到过滤数组中
                endLatLngs.add(originalLatLngs[currentIndex])
                //将原来的线段以当前点为中心拆成两段，分别进行递归处理
                compressLine(originalLatLngs, endLatLngs, start, currentIndex, dMax)
                compressLine(originalLatLngs, endLatLngs, currentIndex, end, dMax)
            }
        }
        return endLatLngs
    }


    /**
     * 使用三角形面积（使用海伦公式求得）相等方法计算点pX到点pA和pB所确定的直线的距离
     * @param start  起始经纬度
     * @param end    结束经纬度
     * @param center 前两个点之间的中心点
     * @return 中心点到 start和end所在直线的距离
     */
    private fun distToSegment(start: LatLngPoint, end: LatLngPoint, center: LatLngPoint): Double {
        val a = Math.abs(AMapUtils.calculateLineDistance(start.latLng, end.latLng)).toDouble()
        val b = Math.abs(AMapUtils.calculateLineDistance(start.latLng, center.latLng)).toDouble()
        val c = Math.abs(AMapUtils.calculateLineDistance(end.latLng, center.latLng)).toDouble()
        val p = (a + b + c) / 2.0
        val s = Math.sqrt(Math.abs(p * (p - a) * (p - b) * (p - c)))
        return s * 2.0 / a
    }


}