package com.lhzw.bluetooth.mvp.presenter

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.WatchInfoBean
import com.lhzw.bluetooth.bean.net.ApkBean
import com.lhzw.bluetooth.bean.net.FirmBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.event.DownLoadEvent
import com.lhzw.bluetooth.mvp.contract.UpdateContract
import com.lhzw.bluetooth.mvp.model.UpdateModel
import com.lhzw.bluetooth.net.rxnet.callback.DownloadCallback
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.kotlinmvp.presenter.BaseIPresenter
import com.orhanobut.logger.Logger
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


/**
 * Date： 2020/7/6 0006
 * Time： 15:11
 * Created by xtqb.
 */

class MainUpdatePresenter : BaseIPresenter<UpdateContract.IView>(), UpdateContract.IPresenter {
    private var mModel: UpdateContract.IModel? = null
    private val TAG = "MainUpdatePresenter"
    private var dfu: FirmBean? = null
    private var apk: ApkBean? = null

    // apk 文件路径
    private val APK_PATH by lazy {
        Environment.getExternalStorageDirectory().toString() + "/apk_file/apk-release.apk"
    }

    // dfu 文件路径
    private val DFU_PATH by lazy {
        Environment.getExternalStorageDirectory().toString() + "/dfu_file/dfu.zip"
    }

    override fun checkUpdate(mContext: Context) {
        // 腕表信息
        val watchInfo = mModel?.queryWatchData()
        if (watchInfo != null && watchInfo.isNotEmpty()) {
            // 说明已连接过手表
            mModel?.getLatestFirm {
                if (it != null) {
                    Log.e(TAG, "${it.getApolloAppVersion()}  ${it.getBleAppVersion()}")
                    var isApolloUpdate = false
                    var isBleUpdate = false
                    dfu = null
                    if (it.getApolloAppVersion() > watchInfo[0].APOLLO_APP_VERSION) {
                        // 说明有新的更新 暂时不支持退版本，仅支持升级
                        isApolloUpdate = true
                        dfu = it
                    }

                    if (it.getBleAppVersion() > watchInfo[0].BLE_APP_VERSION) {
                        isBleUpdate = true
                        dfu = it
                    }
                    var apolloVersion = ""
                    var bleVersion = ""
                    if (dfu == null) {
                        apolloVersion = BaseUtils.apolloOrBleToVersion(watchInfo[0].APOLLO_APP_VERSION)
                        bleVersion = BaseUtils.apolloOrBleToVersion(watchInfo[0].BLE_APP_VERSION)
                    } else {
                        apolloVersion = BaseUtils.apolloOrBleToVersion(it.getApolloAppVersion().toInt())
                        bleVersion = BaseUtils.apolloOrBleToVersion(it.getBleAppVersion().toInt())
                    }
                    mView?.updateFirmState(isApolloUpdate, apolloVersion, isBleUpdate, bleVersion)
                    // 检查Apk
                    checkApkUpdate(mContext) { state, version ->
                        mView?.updateApkState(state, version)
                    }
                } else {
                    Log.e(TAG, "${watchInfo[0].APOLLO_APP_VERSION}    ${watchInfo[0].BLE_APP_VERSION}")
                    mView?.updateFirmState(false, BaseUtils.apolloOrBleToVersion(watchInfo[0].APOLLO_APP_VERSION),
                            false, BaseUtils.apolloOrBleToVersion(watchInfo[0].BLE_APP_VERSION))
                    mView?.updateApkState(false, App.instance
                            .packageManager.getPackageInfo(App.instance.packageName, 0).versionName)
                    Toast.makeText(mContext, "连接超时...", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // 检查Apk
            checkApkUpdate(mContext) { state, version ->
                mView?.updateApkState(state, version)
            }
        }

        // 检查Apk
//        checkApkUpdate(mContext) { state, version ->
//            mView?.updateApkState(state, version)
//    }
    }

    private fun checkApkUpdate(mContext: Context, body: (isUpdate: Boolean, versionName: String) -> Unit) {
        // apk 版本信息
        var appVersionCode: Long = 0
        var appVersionName = ""
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
        mModel?.getLatestApk {
            if (it != null) {
                Log.e(TAG, "${it.getPackageName()}")
                var isApkUpdate = false
                if (appVersionCode < it.getVersionCode()) {
                    isApkUpdate = true
                    //Logger.e("versionCode=${it.getVersionCode()}")
                    apk = it
                }
                body(isApkUpdate, it.getVersionName()!!)
            } else {
//                Toast.makeText(mContext, "无Apk版本可升级", Toast.LENGTH_SHORT).show()
                body(false, appVersionName)
            }
        }
    }

    override fun onAttach() {
        mModel = UpdateModel()
    }


    override fun onDettach() {
        mModel?.onDettach()
        mModel = null
        apk = null
        dfu = null
    }

    override fun initWatchUI() {
        // 腕表信息
        val watchInfo = mModel?.queryWatchData()
        if (watchInfo != null && watchInfo.isNotEmpty()) {
            mView?.initWatchUI(BaseUtils.apolloOrBleToVersion(watchInfo[0].APOLLO_APP_VERSION),
                    BaseUtils.apolloOrBleToVersion(watchInfo[0].BLE_APP_VERSION))
        } else {
            mView?.initWatchUI("", "")
        }
    }

    override fun downloadApk(listener: DownloadCallback) {
        Log.e(TAG, "下载apk...")
        /*
        Log.e("downloadApk", "----------------------------------------------------1")
        apk?.let { bean ->
            Log.e("downloadApk", "----------------------------------------------------2")
            mModel?.downloadApk(bean.getAttachmentId()) { response ->
                Log.e("downloadApk", "----------------------------------------------------3")
                response?.let {
                    Log.e("downloadApk", "----------------------------------------------------4")
                    writeFile(it.body()!!, File(getApkPaht()), mHandler, Constants.DOWNLOAD_APK)
                }
            }
        }
         */
        mModel?.dowloadFile("attachments/apks/${apk?.getAttachmentId()}", getApkPaht(), listener)
    }

    override fun downloadDfu(listener: DownloadCallback) {
        Log.e(TAG, "下载firm...")
        /*
        dfu?.let { bean ->
            mModel?.downloadDfu(bean.getAttachmentId()) { response ->
                response?.let {
                    writeFile(it.body()!!, File(getDfuPaht()), mHandler, Constants.DOWNLOAD_DFU)
                }
            }
        }
         */
        mModel?.dowloadFile("attachments/firms/${dfu?.getAttachmentId()}", getDfuPaht(), listener)
    }

    /**
     * 将输入流写入文件
     * @param inputString
     * @param file
     */
    private fun writeFile(response: ResponseBody, file: File, mHandler: Handler, downloadType: Int) {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        try {
            //文件大小
            val contentLength = response.contentLength()
            //读取文件
            inputStream = response.byteStream()

            if (file.exists()) {
                file.delete()
            }
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs();
            }
            //创建一个文件夹
            outputStream = FileOutputStream(file)
            val bytes = ByteArray(1024)
            var len = 0
            var progress = 0
            val event = DownLoadEvent(contentLength.toInt(), progress)
            val msg = Message()
            msg.what = downloadType
            msg.obj = event
            mHandler.sendMessage(msg)
            //循环读取文件的内容，把他放到新的文件目录里面
            while (inputStream.read(bytes).also { len = it } != -1) {
                outputStream.write(bytes, 0, len)
                val length: Long = file.length()
                //获取下载的大小，并把它传给页面
                progress += len
                val event = DownLoadEvent(contentLength.toInt(), progress)
                val msg = Message()
                msg.what = downloadType
                msg.obj = event
                mHandler.sendMessage(msg)
                Thread.sleep(if (downloadType == Constants.DOWNLOAD_APK) 1 else 5)
            }
//         //当下载完成后，利用粘性发送，并安装
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.flush()
            outputStream?.close()
            inputStream?.close()
        }

    }

    override fun getApkPaht() = APK_PATH
    override fun getDfuPaht() = DFU_PATH

    /**
     * 兼容8.0的安装apk
     * @param mContext
     */
    override fun installApk(mContext: Activity) {
        val filePath = File(getApkPaht())
        if (!filePath.exists()) {
            Toast.makeText(mContext, "安装文件不存在", Toast.LENGTH_SHORT).show()
            return
        }
        if (Build.VERSION.SDK_INT >= 26) {
            val b = mContext.packageManager.canRequestPackageInstalls()
            if (b) {
                mModel?.installApk(mContext, getApkPaht()) {
                    mView?.complete()
                }
                //安装应用的逻辑(写自己的就可以)
            } else {
                //设置安装未知应用来源的权限
                mModel?.startInstallPermissionSettingActivity(mContext)
            }
        } else {
            mModel?.installApk(mContext, getApkPaht()) {
                mView?.complete()
            }
        }
    }

    override fun updateDate() {
        val watchInfo = mModel?.queryWatchData()
        if (watchInfo != null && watchInfo.isNotEmpty() && dfu != null) {
            val values = ContentValues()
            values.put("APOLLO_APP_VERSION", dfu!!.getApolloAppVersion().toInt())
            values.put("BLE_APP_VERSION", dfu!!.getBleAppVersion().toInt())
            CommOperation.update(WatchInfoBean::class.java, values, watchInfo[0].id)
        }
    }
}