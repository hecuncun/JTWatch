package com.lhzw.bluetooth.mvp.contract

import android.app.Activity
import android.content.Context
import com.lhzw.bluetooth.base.BaseIView
import com.lhzw.bluetooth.bean.WatchInfoBean
import com.lhzw.bluetooth.bean.net.ApkBean
import com.lhzw.bluetooth.bean.net.FirmBean
import com.lhzw.bluetooth.net.rxnet.callback.DownloadCallback
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * Date： 2020/7/6 0006
 * Time： 14:58
 * Created by xtqb.
 */
interface UpdateContract {

    interface IModel {
        fun onDettach()
        fun queryWatchData(): List<WatchInfoBean>?

        fun getLatestApk(body: (apk: ApkBean?) -> Unit)

        fun getLatestFirm(body: (firm: FirmBean?) -> Unit)

        fun downloadApk(attachmentId: Long, body: (mResponseBody: Response<ResponseBody>?) -> Unit)

        fun downloadDfu(attachmentId: Long, body: (mResponseBody: Response<ResponseBody>?) -> Unit)

        fun dowloadFile(url: String, path: String, listener: DownloadCallback)

        fun installApk(mContext: Activity, filePath: String?, complete :()-> Unit)

        fun startInstallPermissionSettingActivity(mContext: Activity)
    }

    interface IView : BaseIView {
        fun updateApkState(state: Boolean, versionName: String)
        fun updateFirmState(apollo: Boolean, apolloVersionName: String, ble: Boolean, bleVersion: String)
        fun initWatchUI(apolloVersion: String, bleVersion: String)
        fun complete()
    }

    interface IPresenter {
        fun checkUpdate(mContext: Context)
        fun onAttach()
        fun onDettach()
        fun initWatchUI()
        fun downloadApk(listener: DownloadCallback)
        fun downloadDfu(listener: DownloadCallback)
        fun getApkPaht(): String
        fun getDfuPaht(): String
        fun installApk(mContext: Activity)
        fun updateDate()
    }
}
