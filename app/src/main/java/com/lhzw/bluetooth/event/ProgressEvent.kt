package com.lhzw.bluetooth.event

/**
 * Date： 2020/7/1 0001
 * Time： 16:59
 * Created by xtqb.
 */
data class ProgressEvent(/* 进度条百分比 */val progress: Float, /*  进度条状态 0 运动数据同步  1 运动数据解析  2 同步解析结束 3 进程杀死/断开连接销毁进度  4 解析失败*/val state: Int)