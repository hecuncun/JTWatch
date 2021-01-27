package com.lhzw.bluetooth.bean

import com.lhzw.bluetooth.uitls.BaseUtils
import org.litepal.crud.LitePalSupport

/**
 *
@author：created by xtqb
@description:  手表信息
@date : 2020/1/8 14:50
 *
 */
data class WatchInfoBean(
        /*     响应号     */
        val response: String,
        /*   蓝牙BOOT版本号   */
        var BLE_BOOT_VERSION: Int,
        /*   蓝牙APP版本号    */
        var BLE_APP_VERSION: Int,
        /*   蓝牙协议栈版本号    */
        var BLE_SOFTDEVICE_VERSION: Int,
        /*   APOLLO BOOT版本号    */
        var APOLLO_BOOT_VERSION: Int,
        /*   APOLLO APP版本号    */
        var APOLLO_APP_VERSION: Int,
        /*   APOLLO UPDATE版本号    */
        var APOLLO_UPDATE_VERSION: Int,
        /*   APOLLO硬件版本号    */
        var APOLLO_HW_VERSION: Int,
        /*   NRF硬件版本号    */
        var NRF_HW_VERSION: Int,
        /*   APOLLO CHIP ID 1    */
        var CHIP_ID_1: Int,
        /*   APOLLO CHIP ID 2    */
        var CHIP_ID_2: Int,
        /*   设备名称    */
        var DEVICE_NAME: String,
        /*   MAC地址    */
        var MAC_ADDRESS: String
) : LitePalSupport() {
    val id: Long = 0

    companion object {
        fun createBean(content: ByteArray?): WatchInfoBean? {
            content?.let {
                var bytes = content.toList()
                val response = bytes[0].toInt().toString()
                val BLE_BOOT_VERSION = BaseUtils.byteToInt(bytes.subList(1, 5))
                val BLE_APP_VERSION = BaseUtils.byteToInt(bytes.subList(5, 9))
                val BLE_SOFTDEVICE_VERSION = BaseUtils.byteToInt(bytes.subList(9, 13))
                val APOLLO_BOOT_VERSION = BaseUtils.byteToInt(bytes.subList(13, 17))
                val APOLLO_APP_VERSION = BaseUtils.byteToInt(bytes.subList(17, 21))
                val APOLLO_UPDATE_VERSION = BaseUtils.byteToInt(bytes.subList(21, 25))
                val APOLLO_HW_VERSION = BaseUtils.byteToInt(bytes.subList(25, 29))
                val NRF_HW_VERSION = BaseUtils.byteToInt(bytes.subList(29, 33))
                val CHIP_ID_1 = BaseUtils.byteToInt(bytes.subList(33, 37))
                val CHIP_ID_2 = BaseUtils.byteToInt(bytes.subList(37, 41))
                val deviceNameL = bytes.size - 1 - 4 * 10 - 6
                val DEVICE_NAME = String(bytes.subList(41, 41 + deviceNameL).toByteArray())
                val mac = bytes.subList(41 + deviceNameL, 47 + deviceNameL).toByteArray()
                var builder = StringBuilder()
                val MAC_ADDRESS = BaseUtils.byte2HexStr(mac)?.replace(" ", ":")!!
                val bean = WatchInfoBean(bytes[0].toString(),
                        BLE_BOOT_VERSION,
                        BLE_APP_VERSION,
                        BLE_SOFTDEVICE_VERSION,
                        APOLLO_BOOT_VERSION,
                        APOLLO_APP_VERSION,
                        APOLLO_UPDATE_VERSION,
                        APOLLO_HW_VERSION,
                        NRF_HW_VERSION,
                        CHIP_ID_1,
                        CHIP_ID_2,
                        DEVICE_NAME,
                        MAC_ADDRESS)
                return bean
            }
            return null
        }
    }
    fun setApolloVer(version : Int){
        this.APOLLO_APP_VERSION = version
    }

    fun setBleVer(version : Int){
        this.BLE_APP_VERSION = version
    }
}