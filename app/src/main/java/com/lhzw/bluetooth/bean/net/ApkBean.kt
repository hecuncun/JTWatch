package com.lhzw.bluetooth.bean.net

/**
 * Date： 2020/7/1 0001
 * Time： 16:17
 * Created by xtqb.
 */
class ApkBean {
    // apk 报名
    private var packageName: String? = null

    // 版本号
    private var versionCode: Long = 0

    // 固件下载标识
    private var attachmentId: Long = 0

    // 版本信息
    private var versionName: String? = null


    fun getPackageName() = packageName

    fun getVersionCode() = versionCode

    fun getAttachmentId() = attachmentId

    fun getVersionName() = versionName
}