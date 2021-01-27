package com.lhzw.bluetooth.mvp.presenter

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.base.BasePresenter
import com.lhzw.bluetooth.mvp.contract.SettingContract
import com.lhzw.bluetooth.mvp.model.SettingModel

/**
 * Created by hecuncun on 2019/11/13
 */
class SettingPresenter : BasePresenter<SettingContract.Model, SettingContract.View>(), SettingContract.Presenter {
    private val TAG = "SettingPresenter"
    override fun createModel(): SettingContract.Model? = SettingModel()

    override fun getPersonalInfo() {
        mView?.getPersonalInfoSuccess(mModel?.getPersonalInfo()) // 回传view
    }

    override fun checkUpdate(mContext: Context) { // 获取腕表数据
        // 腕表信息
        val watchInfo = mModel?.queryWatchData()
        if (watchInfo != null && watchInfo.isNotEmpty()) {
            // 说明已连接过手表
            mModel?.getLatestFirm {
                if (it != null) {
                    Log.e(TAG, "${it.getApolloAppVersion()}  ${it.getBleAppVersion()}")
                    var isFirmUpdate = false
                    if (it.getApolloAppVersion() > watchInfo[0].APOLLO_APP_VERSION || it.getBleAppVersion() > watchInfo[0].BLE_APP_VERSION) {
                        // 说明有新的更新 暂时不支持退版本，仅支持升级
                        isFirmUpdate = true
                    }

                    // 检查Apk
                    checkApkUpdate(mContext) {
                        if (isFirmUpdate || it) {
                            mView?.refleshUpdateState(true)
                        } else {
                            mView?.refleshUpdateState(false)
                        }
                    }
                } else {
//                    Toast.makeText(mContext, "无腕表固件版本可升级", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // 检查Apk
            checkApkUpdate(mContext) {
                if (it) {
                    mView?.refleshUpdateState(true)
                } else {
                    mView?.refleshUpdateState(false)
                }
            }
        }


    }

    private fun checkApkUpdate(mContext: Context, body: (isUpdate: Boolean) -> Unit) {
        // apk 版本信息
        var appVersionCode: Long = 0
        var appVersionName: String = ""
        try {
            val packageInfo = App.instance
                    .packageManager
                    .getPackageInfo(App.instance.packageName, 0)
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            appVersionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, e.message)
        }
        mModel?.getLatestApk() {
            if (it != null) {
                Log.e(TAG, "${it.getPackageName()}")
                var isApkUpdate = false
                if (appVersionCode < it.getVersionCode()) {
                    isApkUpdate = true
                }
                body(isApkUpdate)
            } else {
//                Toast.makeText(mContext, "无Apk版本可升级", Toast.LENGTH_SHORT).show()
                body(false)
            }
        }
    }
}