package com.lhzw.bluetooth.bean

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.uitls.BaseUtils
import org.litepal.crud.LitePalSupport

/**
 *
@author：created by xtqb
@description:
@date : 2020/1/15 14:03
 *
 */
data class SportDetailInfobean(
        /*    活动详情标识 即根据该标记查询活动详情  sport_detail_mark = activity_start-activity_end 组成一个字符串，中间用“-”分开  （注：保留）*/
        var sport_detail_mark: String,
        /*     数据类型     包括 计步 心率  气压  gps 距离  热量  速度  距离标点*/
        var data_type: Int,
        /*     数据偏移位置  0 -- 999  */
        var data_index: Int,
        /*     数据之间的时间间隔  1分钟或者五分钟   */
        var sector_time: Int,
        /*     通用数据类型  根据不同数据类型有不同的数据解析 */
        var value: Int,
        /*      附加数据 高度 ，高度为附加数据*/
        var addition: Int,
        /*     纬度    （注：仅在gps类型有数据）*/
        var gps_latitude: Double,
        /*     经度    （注：仅在gps类型有数据）*/
        var gps_longitude: Double
) : LitePalSupport() {
    val id: Long = 0

    companion object {
        fun parserSportDetailInfo(map: HashMap<String, HashMap<Int, MutableList<Byte>>>, body: () -> Unit) {
            map.forEach { (mark, data) ->
                deleteData(mark) //如果数据表中有该mark数据，进行删除
                data.forEach { (type, content) ->
                    val read_len = content.size
                    when (type) {
                        // 除了Gps 4字节数据  活动步数 单位步 1分钟 活动距离 单位 cm  热量 单位 卡 一分钟 速度 单位 m/s 一分钟
                        Constants.STEP, Constants.DISTANCE, Constants.CALORIE -> {
                            parserData(mark, type, 4, 1, content, read_len)
                            body()
                        }
                        // 一个字节  一分钟  心率
                        Constants.HEART_RATE -> {
                            parserData(mark, type, 1, 1, content, read_len)
                            body()
                        }
                        // 4字节 气压 单位帕  高度4字节浮点数  高度米 5分钟
                        Constants.AIR_PRESSURE -> {
                            parserAirPressure(mark, type, 8, 5, content, read_len)
                            body()
                        }
                        // 8个字节数据 经纬度各占四个字节，带符号整形数据 然后在除 1000000  高精度 1s 低电量 1s或者5s
                        Constants.GPS -> {
//                            Log.e("parserdetail", "$mark  $type  ${content.size}  ${BaseUtils.byte2HexStr(content.toByteArray())}")
                            parserGps(mark, type, 8, 1, content, read_len)
                            body()
                        }
                        Constants.SPEED -> {
                            parserData(mark, type, 2, 1, content, read_len)
                            body()
                        }
                    }
                }
            }
        }

        // 解析通用数据
        private fun parserData(mark: String, type: Int, interval: Int, sector_time: Int, content: List<Byte>, len: Int) {
            for (index in 0 until len / interval) {
//                Log.e("parserData", "type : $type  ${BaseUtils.byte2HexStr(content.subList(index * interval, (index + 1) * interval).toByteArray())}")
                val bean = SportDetailInfobean(
                        mark,
                        type,
                        index,
                        sector_time,
                        BaseUtils.byteToInt(content.subList(index * interval, (index + 1) * interval)),
                        0,
                        0.0,
                        0.0
                )
                CommOperation.insert(bean)
            }
        }

        // 解析海拔数据 气压和海拔
        private fun parserAirPressure(mark: String, type: Int, interval: Int, sector_time: Int, content: List<Byte>, len: Int) {
            for (index in 0 until len / interval) {
                val bean = SportDetailInfobean(
                        mark,
                        type,
                        index,
                        sector_time,
                        BaseUtils.byteToInt(content.subList(index * interval, index * interval + interval / 2)),
                        BaseUtils.byteToInt(content.subList(index * interval + interval / 2, (index + 1) * interval)),
                        0.0,
                        0.0
                )
                CommOperation.insert(bean)
            }
        }

        // 解析Gps数据 只存储大于0的数据
        private fun parserGps(mark: String, type: Int, interval: Int, sector_time: Int, content: List<Byte>, len: Int) {
            var isStart = false
            var tmp = LatLng(0.0, 0.0)
            var conter = 0
            val total = len / interval
            var index = 0
            while (index < total) {
                var lat = BaseUtils.byteToInt(content.subList(index * interval + interval / 2, (index + 1) * interval)).toDouble()
                var lgt = BaseUtils.byteToInt(content.subList(index * interval, index * interval + interval / 2)).toDouble()
//                Log.e("GPS_LATLGT", "mark  ${mark}  lat : ${lat}, lgt  ${lgt}")
                if (lat > 0.0 && lgt > 0.0) {
                    var start = BaseUtils.gps84_To_Gcj02(lat / 100000, lgt / 100000)
                    if (!isStart) {
                        if (conter > 0) {
                            conter--
                        } else {
                            // 第一个点或者出现零点时重新定位基准点
                            isStart = true
                            tmp = LatLng(start[0], start[1])
                            val bean = SportDetailInfobean(
                                    mark,
                                    type,
                                    index,
                                    sector_time,
                                    0,
                                    0,
                                    start[0],
                                    start[1])
                            CommOperation.insert(bean)
                        }
                    } else {
                        var latLng = LatLng(start[0], start[1])
                        val distance = AMapUtils.calculateLineDistance(latLng, tmp)
                        if (distance > 5.0 && distance <= 50.0) {
                            tmp = latLng
                            val bean = SportDetailInfobean(
                                    mark,
                                    type,
                                    index,
                                    sector_time,
                                    0,
                                    0,
                                    start[0],
                                    start[1])
                            CommOperation.insert(bean)
                        } else {
                            if (distance > 50) {
                                conter = Constants.FILTER_CONTER
                                if (index + 4 < total) {
                                    var currentPos = index + 1
                                    var isGreater = true
                                    while (conter > 0 && isGreater) {
                                        var lat = BaseUtils.byteToInt(content.subList(currentPos * interval + interval / 2, (currentPos + 1) * interval)).toDouble()
                                        var lgt = BaseUtils.byteToInt(content.subList(currentPos * interval, currentPos * interval + interval / 2)).toDouble()
                                        start = BaseUtils.gps84_To_Gcj02(lat / 100000, lgt / 100000)
                                        latLng = LatLng(start[0], start[1])
                                        val distance = AMapUtils.calculateLineDistance(latLng, tmp)
                                        if (distance < 50.0) {
                                            isGreater = false;
                                        } else {
                                            conter--
                                            currentPos++
                                        }
                                    }
                                    tmp = latLng
                                    val bean = SportDetailInfobean(
                                            mark,
                                            type,
                                            index,
                                            sector_time,
                                            0,
                                            0,
                                            start[0],
                                            start[1])
                                    CommOperation.insert(bean)
                                    index = currentPos
                                    conter = 0
                                } else {
                                    // 直接跳出
                                    index = total
                                }
                            }
                        }
                    }
                } else {
                    if (isStart) {
                        isStart = false
                        conter = 7
                    }
                }
                index++
            }
        }

        //删除数据
        private fun deleteData(mark: String) {
            CommOperation.delete(SportDetailInfobean::class.java, "sport_detail_mark", mark)
        }
    }
}
