package com.lhzw.bluetooth.bean.net

/**
 * Date： 2020/7/1 0001
 * Time： 16:17
 * Created by xtqb.
 */
class FirmBean {
    // 固件类型
    private var model: String? = null

    // 版本号
    private var bleAppVersion: Long = 0

    // 固件下载标识
    private var attachmentId: Long = 0

    // 版本信息
    private var apolloAppVersion: Long = 0


    fun getModel() = model

    fun getBleAppVersion() = bleAppVersion

    fun getAttachmentId() = attachmentId

    fun getApolloAppVersion() = apolloAppVersion

}