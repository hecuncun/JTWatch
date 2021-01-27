package com.lhzw.bluetooth.constants

import android.graphics.Color
import android.graphics.Typeface
import android.net.wifi.p2p.WifiP2pManager
import com.lhzw.bluetooth.application.App

/**
 *
@author：created by xtqb
@description:
@date : 2019/11/6 9:16
 *
 */

object Constants {
    const val INFO_CHANGE = "info_change"
    const val PHOTO_PATH = "photo_path"
    const val IVBACKGROUND = "iv_background"
    const val INDOOR_BACKGROUD = "indoor_backgroud"

    const val APK_UPDATE_TIME = "apk_update_time"
    const val FIRM_UPDATE_TIME = "firm_update_time"
    const val APK_IP_CHANGE = "apk_ip_change"
    const val REGISTERTIME = "registertime"

    const val IS_AGREE="is_agree"


    // 运动类型
    const val ACTIVITY_RUNNING = 0xB1  //跑步
    const val ACTIVITY_REDING = 0xB2    //骑车
    const val ACTIVITY_HIKING = 0xB3    //徒步
    const val ACTIVITY_CLIMBING = 0xB4    //登山
    const val ACTIVITY_INDOOR = 0xB5    //室内跑
    const val ACTIVITY_ALL = 0 //全部

    //如果设置了target > 28，需要增加这个权限，否则不会弹出"始终允许"这个选择框
    const val BACK_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION"
    const val REQUESTCODE = 0 //权限获取请求code

    const val ZOOM = 15.5F //地图放大级别
    const val LGT = 116.337000
    const val LAT = 39.977290

    // 绘制轨迹起始终止颜色值
    var startColor = Color.argb(255, 0, 255, 255)
    var endColor = Color.argb(255, 241, 90, 36)

    //包名唯一标示
    const val WEIXIN = "com.tencent.mm"
    const val QQ = "com.tencent.mobileqq"
    const val SINA = "com.sina.weibo"
    const val MMS = "com.android.mms"
    const val CALL_COMING = "com.android.incallui"
    const val CALL = "com.android.server.telecom"
    const val CALL_CONTACT = "com.android.contacts"

    //记录定位中心点经纬度
    const val CENTER_LAT = "CENTER_LAT"
    const val CENTER_LGT = "CENTER_LGT"

    // 服务包名
    const val SERVICE_PACKAGE = "com.lhzw.bluetooth.service.BlutoothService"
    const val SMS_AND_PHONE_SERVICE_PACKAGE = "com.lhzw.bluetooth.service.SmsAndPhoneService"
    const val BLE_CONNECT_SERVICE_PACKAGE = "com.lhzw.bluetooth.service.BleConnectService"


    const val CONNECT_STATE = "connect_state"
    const val CONNECT_DEVICE_NAME = "connect_device_name"
    const val LAST_DEVICE_MAC_ADDRESS = "last_device_mac_address"
    const val ISSYNCASCENDING = "is_sync_ascending"
    const val SYNC_TIME = "sync_time"
    const val LAST_CONNECTED_ADDRESS = "last_connected_address"
    const val AUTO_CONNECT = "auto_connect"
    const val ACCEPT_MSG = "accept_msg"

    const val FILTER_CONTER = 4


    const val CONNECT_RESPONSE_CODE: Byte = 0x01//连接参数更新
    const val MTU_RESPONSE_CODE: Byte = 0x03//MTU更新
    const val UPDATE_TIME_RESPONSE_CODE: Byte = 0x07//手表时间更新
    const val UPDATE_PERSON_INFO_RESPONSE_CODE: Byte = 0X09//更新用户个人信息
    const val CURRENT_DATA_UPDATE_RESPONSE_CODE: Byte = 0X10//动态数据更新
    const val UPDATE_DAILY_INFO_RESPONSE_CODE: Byte = 0X0C//动态数据更新
    const val SEND_PHONE_RESPONSE_CODE: Byte = 0X0F// 推送消息回传号 0x0f


    const val MTU_MAX = 244 - 11//动态数据更新

    //刷新类型
    const val TYPE_CURRENT_DATA = "type_current_data"
    const val BIRTHDAY = "birthday"
    const val NICK_NAME = "nick_name"
    const val IS_AGREEMENT = "is_agreement"

    //数据类型
    const val STEP = 0;         // 计步     四个字节    单位  步      每分钟记录一次
    const val HEART_RATE = 1;   // 心率     一个字节    单位  次/分   每分钟记录一次
    const val AIR_PRESSURE = 2  // 气压    8个字节  前四个字节为气压  单位 帕   后四个字节 为海拔 单位 米  每5分钟记录一次
    const val GPS = 3;          // gps     8个字节  前四个字节为经度 后四个字节为维度 得到的值除以1000000为实际的值
    const val DISTANCE = 4      // 距离     4个字节   单位 cm    每分钟记录一次
    const val CALORIE = 5       // 热量     4个字节   单位卡     每分钟记录一次
    const val SPEED = 6         // 速度     2个字节    单位 m/s  每分钟记录一次

    // 手表数据存储单位时间 单位分钟
    const val UNIT_5 = 5
    const val UNIT_1 = 1
    const val TOTAL = 7 //共计数据

    //消息通知开关
    const val TYPE_PHONE = "type_phone"
    const val TYPE_MSG = "type_msg"
    const val TYPE_QQ = "type_qq"
    const val TYPE_WX = "type_wx"

    // 坐标系转换参数
    val PI = 3.1415926535897932384626
    val A = 6378245.0
    val EE = 0.00669342162296594323

    //  活动列表
    val ACTIVITIES = arrayOf(
            ACTIVITY_RUNNING,
            ACTIVITY_REDING,
            ACTIVITY_HIKING,
            ACTIVITY_CLIMBING,
            ACTIVITY_INDOOR
    )

    // 字体
    val font_futurn_num = Typeface.createFromAsset(App.context.assets, "fonts/futurn_num.ttf")
    val font_normal = Typeface.createFromAsset(App.context.assets, "fonts/fuji_normal.ttf")

//    const val IP_ADD = "http://192.168.0.101:8084/"
//    const val IP_ADD = "http://120.26.83.121:8084/"
    const val IP_ADD = "http://www.cetcjt.cn/"
    const val HTTP_TOOKEN = "x-access-token"
    const val HTTP_CACHE_TOOKEN = "http_cache_token"
    const val FIREST_LOGIN = "FIREST_LOGIN"

    const val TAP_HOME = 0
    const val TAP_SPORTS = 1
    const val TAP_WATCH = 2
    const val TAP_SETTING = 3
    const val TAP_ME = 4

    // 当前腕表类型 暂时
    const val WATCH_TYPE = "sw2500"

    const val DOWNLOAD_APK = 0
    const val DOWNLOAD_DFU = 5

    const val VERIFY_TYPE_REGISTER = 0x0000 // 注册
    const val VERIFY_TYPE_MODIFY_PASSWORD = 0x0001 // 更改密码
    const val APP_ID = "wx1fd7f0ff7df35c16"
    const val USER_ID = "wx17ab04a8ad941271"

}