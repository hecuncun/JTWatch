package com.lhzw.bluetooth.ble

/**
 *
@author：created by xtqb
@description:
@date : 2020/1/14 16:21
 *
 */
data class ReadSportAcitvityBean<T>(
        /*    命令号     */
        var request_code: Byte,
        /*    读取的数据     */
        var list: List<T>,
        /*    请求时间   */
        var request_date: ByteArray,
        /*    请求的活动号   */
        var request_mark: Byte,
        /*    第几个数据     */
        var bean_index: Int,
        /*    第几个活动     */
        var activity_index: Int,
        /*     是否结束   */
        var isOver: Boolean
)