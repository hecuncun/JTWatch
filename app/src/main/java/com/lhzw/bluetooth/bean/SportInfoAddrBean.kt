package com.lhzw.bluetooth.bean

import android.util.Log
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.uitls.BaseUtils
import org.litepal.crud.LitePalSupport

/**
 *
@author：created by xtqb
@description: 运动数据
@date : 2019/11/18 11:11
 *
 */

data class SportInfoAddrBean(
        /*    日期标识  = 日常日期-活动号   */
        val daily_date_mark: String,
        /*    活动类型     */
        var activity_type: Int,
        /*    活动序号     */
        var activity_mark: Int,
        /*    活动删除标志     */
        var activity_delete_mark: Int,
        /*    活动开始时间     */
        var activity_start: Long,
        /*    活动结束时间     */
        var activity_end: Long,
        /*    活动开始\结束时间对  共计最大10组  （注： 暂不实现）   共计字节数 3 * 2 * 10 = 60字节*/
        var activity_start_or_end: String,
        /*    计步开始地址     */
        var step_start_addr: Int,
        /*    计步结束地址     */
        var step_end_addr: Int,
        /*    心率开始地址     */
        var heart_rate_start_addr: Int,
        /*    心率结束地址     */
        var heart_rate_end_addr: Int,
        /*    气压开始地址     */
        var air_pressure_start_addr: Int,
        /*    气压结束地址     */
        var air_pressure_end_addr: Int,
        /*    GPS开始地址     */
        var gps_start_addr: Int,
        /*    GPS结束地址     */
        var gps_end_addr: Int,
        /*    距离开始地址     */
        var distance_start_addr: Int,
        /*    距离结束地址     */
        var distance_end_addr: Int,
        /*    热量开始地址     */
        var calorie_start_addr: Int,
        /*    热量结束地址     */
        var calorie_end_addr: Int,
        /*    速度开始地址     */
        var speed_start_addr: Int,
        /*    速度结束地址     */
        var speed_end_addr: Int,
        /*    距离标点开始地址    （注：保留） */
        var distance_point_start_addr: Int,
        /*    距离标点结束地址     （注：保留）*/
        var distance_point_end_addr: Int,
        /*    保留   24个字节数据 （注：保留） */
        var reverse: String,
        /*    活动详情标识 即根据该标记查询活动详情  sport_detail_mark = activity_start-activity_end 组成一个字符串，中间用“-”分开  （注：保留）*/
        var sport_detail_mark: String

) : LitePalSupport() {
    val id: Long = 1  //uniquen

    companion object {
        fun parserSportInfoAddr(content: ByteArray?, ID: String, body: (data: ByteArray, mark: Byte, bean : SportInfoAddrBean) -> Unit) {
            content?.let {
                val list = CommOperation.query(SportInfoAddrBean::class.java, "daily_date_mark", ID)
                if (list.isNotEmpty()) {
                    CommOperation.delete(SportInfoAddrBean::class.java, "daily_date_mark", ID)
                }
                val activity_type = (content[1].toInt() and 0xFF).toInt()
                val activity_mark = content[2].toInt()
                val activity_delete_mark = content[3].toInt()
                val activity_start = BaseUtils.byteToLong(content.copyOfRange(4, 10).toList())
                val activity_end = BaseUtils.byteToLong(content.copyOfRange(10, 16).toList())
                // 活动开始\结束时间对 暂不实现
                val step_start_addr = BaseUtils.byteToInt(content.copyOfRange(76, 80).toList())
                val step_end_addr = BaseUtils.byteToInt(content.copyOfRange(80, 84).toList())
                val heart_rate_start_addr = BaseUtils.byteToInt(content.copyOfRange(84, 88).toList())
                val heart_rate_end_addr = BaseUtils.byteToInt(content.copyOfRange(88, 92).toList())
                val air_pressure_start_addr = BaseUtils.byteToInt(content.copyOfRange(92, 96).toList())
                val air_pressure_end_addr = BaseUtils.byteToInt(content.copyOfRange(96, 100).toList())
                val gps_start_addr = BaseUtils.byteToInt(content.copyOfRange(100, 104).toList())
                val gps_end_addr = BaseUtils.byteToInt(content.copyOfRange(104, 108).toList())
                val distance_start_addr = BaseUtils.byteToInt(content.copyOfRange(108, 112).toList())
                val distance_end_addr = BaseUtils.byteToInt(content.copyOfRange(112, 116).toList())
                val calorie_start_addr = BaseUtils.byteToInt(content.copyOfRange(116, 120).toList())
                val calorie_end_addr = BaseUtils.byteToInt(content.copyOfRange(120, 124).toList())
                val speed_start_addr = BaseUtils.byteToInt(content.copyOfRange(124, 128).toList())
                val speed_end_addr = BaseUtils.byteToInt(content.copyOfRange(128, 132).toList())
                val distance_point_start_addr = BaseUtils.byteToInt(content.copyOfRange(132, 136).toList())
                val distance_point_end_addr = BaseUtils.byteToInt(content.copyOfRange(136, 140).toList())
                //保留24个字节
                val sport_detail_mark = "$ID-$activity_start-$activity_end"

                Log.e("parserdetail", "$activity_mark  $  ${sport_detail_mark}  ${BaseUtils.byte2HexStr(content)}")
                val bean = SportInfoAddrBean(
                        ID,
                        activity_type,
                        activity_mark,
                        activity_delete_mark,
                        activity_start,
                        activity_end,
                        "",
                        step_start_addr,
                        step_end_addr,
                        heart_rate_start_addr,
                        heart_rate_end_addr,
                        air_pressure_start_addr,
                        air_pressure_end_addr,
                        gps_start_addr,
                        gps_end_addr,
                        distance_start_addr,
                        distance_end_addr,
                        calorie_start_addr,
                        calorie_end_addr,
                        speed_start_addr,
                        speed_end_addr,
                        distance_point_start_addr,
                        distance_point_end_addr,
                        "",
                        sport_detail_mark
                )
                // 如果存在直接删除插入新数据
                if (activity_type == Constants.ACTIVITY_CLIMBING) {
                    val list = CommOperation.query(ClimbingSportBean::class.java, "sport_detail_mark", sport_detail_mark)
                    if (list.isNotEmpty()) {
                        CommOperation.delete(ClimbingSportBean::class.java, "sport_detail_mark", sport_detail_mark)
                    }
                } else {
                    val list = CommOperation.query(FlatSportBean::class.java, "sport_detail_mark", sport_detail_mark)
                    if (list.isNotEmpty()) {
                        CommOperation.delete(FlatSportBean::class.java, "sport_detail_mark", sport_detail_mark)
                    }
                }
                // 保存运动详情数据
                val step_num = BaseUtils.byteToInt(content.copyOfRange(164, 168).toList())
                val distance = BaseUtils.byteToInt(content.copyOfRange(168, 172).toList())
                val calorie = BaseUtils.byteToInt(content.copyOfRange(172, 176).toList())

                val warmup_heart_rate = BaseUtils.byteToInt(content.copyOfRange(188, 192).toList())  // 热身
                val flaming_heart_rate = BaseUtils.byteToInt(content.copyOfRange(192, 196).toList())   // 燃脂
                val aerobic_heart_rate = BaseUtils.byteToInt(content.copyOfRange(196, 200).toList())    // 有氧
                val limitation_heart_rate = BaseUtils.byteToInt(content.copyOfRange(200, 204).toList())    //  极限
                val anaerobic_heart_rate = BaseUtils.byteToInt(content.copyOfRange(204, 208).toList())     //  无氧
                if (activity_type == Constants.ACTIVITY_CLIMBING) {
                    val vertical_speed = BaseUtils.byteToInt(content.copyOfRange(176, 178).toList())
                    val lifting_height = BaseUtils.byteToInt(content.copyOfRange(178, 180).toList())
                    val drop_height = BaseUtils.byteToInt(content.copyOfRange(180, 182).toList())
                    val average_heart_rate = content[182].toInt() and 0xFF
                    val max_heart_rate = content[183].toInt() and 0xFF
                    val climbing = ClimbingSportBean(
                            sport_detail_mark,
                            step_num,
                            distance,
                            calorie,
                            vertical_speed,
                            lifting_height,
                            drop_height,
                            average_heart_rate,
                            max_heart_rate,
                            limitation_heart_rate,
                            anaerobic_heart_rate,
                            aerobic_heart_rate,
                            flaming_heart_rate,
                            warmup_heart_rate
                    )
                    CommOperation.insert(climbing)
                } else {
                    val speed = BaseUtils.byteToInt(content.copyOfRange(176, 178).toList())
                    val best_speed = BaseUtils.byteToInt(content.copyOfRange(178, 180).toList())
                    val average_heart_rate = content[180].toInt() and 0xFF
                    val max_heart_rate = content[181].toInt() and 0xFF
                    // 保留两位
                    val flatSport = FlatSportBean(
                            sport_detail_mark,
                            step_num,
                            distance,
                            calorie,
                            speed,
                            best_speed,
                            average_heart_rate,
                            max_heart_rate,
                            "",
                            limitation_heart_rate,
                            anaerobic_heart_rate,
                            aerobic_heart_rate,
                            flaming_heart_rate,
                            warmup_heart_rate
                    )
                    CommOperation.insert(flatSport)
                }
                CommOperation.insert(bean)
                body(content.copyOfRange(10, 16).toList().toByteArray(), content[2],bean)
            }
        }
    }
}