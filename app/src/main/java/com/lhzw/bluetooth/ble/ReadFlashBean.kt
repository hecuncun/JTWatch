package com.lhzw.bluetooth.ble

/**
 *
@author：created by xtqb
@description:
@date : 2020/1/10 14:27
 *
 */
data class ReadFlashBean<T>(
        /*    读取的数据     */
        var list: List<T>,
        /*    第几个数据     */
        var index: Int,
        /*    回传号     */
        var request_code: Byte,
        /*    当前读取的数据起始地址     */
        var current_addr: Int,
        /*    当前读取的数据长度     */
        var read_len: Int,
        /*    读取的第几个MUT     */
        var counter_mtu: Int,
        /*     是否结束   */
        var isOver: Boolean
)