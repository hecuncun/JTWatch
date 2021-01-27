package com.lhzw.bluetooth.ble

/**
 *
@author：created by xtqb
@description:
@date : 2020/1/17 14:13
 *
 */
data class ReadDetailFlashBean(
        /*    回传号     */
        var request_code: Byte,
        /*    活动详情标识 即根据该标记查询活动详情  sport_detail_mark = activity_start-activity_end 组成一个字符串，中间用“-”分开  （注：保留）*/
        var sport_detail_mark: String,
        /*    读取的数据类型     */
        var data_type: Int,
        /*    当前读取的数据起始地址     */
        var current_addr: Int,
        /*    当前读取的数据长度     */
        var read_len: Int
)