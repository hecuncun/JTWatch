package com.lhzw.bluetooth.bean

import org.litepal.crud.LitePalSupport

/**
 *
@author：created by xtqb
@description: 平坦运动，包括 跑步，徒步，市内跑，骑行
@date : 2020/1/15 9:15
 *
 */
data class ClimbingSportBean(
        /*    活动详情标识 即根据该标记查询活动详情  sport_detail_mark = activity_start-activity_end 组成一个字符串，中间用“-”分开  （注：保留）*/
        var sport_detail_mark: String,
        /*    步数     */
        var step_num: Int,
        /*    距离     */
        var distance: Int,
        /*    卡路里     */
        var calorie: Int,
        /*    垂直高度     */
        var vertical_speed: Int,
        /*    上升高度     */
        var lifting_height: Int,
        /*    下降高度     */
        var drop_height: Int,
        /*    平均心率   */
        var average_heart_rate: Int,
        /*    最大心率   */
        var max_heart_rate: Int,
        /*    极限心率区间时间   */
        var limitation_heart_rate: Int,
        /*    无氧心率区间时间   */
        var anaerobic_heart_rate: Int,
        /*    有氧心率区间时间   */
        var aerobic_heart_rate: Int,
        /*    燃脂心率区间时间   */
        var flaming_heart_rate: Int,
        /*    热身心率区间时间   */
        var warmup_heart_rate: Int
) : LitePalSupport() {
    val id: Long = 0
}