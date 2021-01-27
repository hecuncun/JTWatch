package com.lhzw.bluetooth.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import com.hwangjr.rxbus.annotation.Subscribe
import com.hwangjr.rxbus.annotation.Tag
import com.hwangjr.rxbus.thread.EventThread
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.base.BaseUpdateActivity
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.mvp.presenter.MainUpdatePresenter
import com.lhzw.bluetooth.net.rxnet.callback.DownloadCallback
import com.lhzw.bluetooth.net.rxnet.utils.LogUtils
import com.lhzw.bluetooth.widget.LoadingView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_update_func_list.*
import java.io.File

/**
 * Date： 2020/6/18 0018
 * Time： 10:42
 * Created by xtqb.
 */

class UpdateFuncActivity : BaseUpdateActivity<MainUpdatePresenter>() {
    private val TAG = UpdateFuncActivity::class.java.simpleName
    private val PERMISS_REQUEST_CODE = 0x0001
    private val UPDATE_WATCH = 0x0015
    private val DOWNLOAD_FIRM = 0x0020
    private var update_type = DOWNLOAD_FIRM
    private var loadingView: LoadingView? = null
    private val DOWNLOADING = 0
    private val COMPLETE = 3
    private val UPDATEFIRM = 1
    private val FREE = 2
    private var state = FREE
    private var status: String? = null
    override fun attachLayoutRes() = R.layout.activity_update_func_list

    @SuppressLint("InvalidWakeLockTag")
    override fun initData() {
        // 初始化界面
        tv_app_update_date.text = apk_update_time
        tv_watch_update_date.text = firm_update_time
        mPresenter?.initWatchUI()
        tv_app_version.text = "JIANGTAI ${
            App.instance
                    .packageManager.getPackageInfo(App.instance.packageName, 0).versionName
        }"
//        checkPermission()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        checkVersion()
    }

    override fun initView() {
        super.initView()
    }

    private fun checkInstall(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val b = packageManager.canRequestPackageInstalls()
            if (!b) {
//                requestPermission(arrayOf(Manifest.permission.REQUEST_INSTALL_PACKAGES), PERMISS_REQUEST_CODE)
                val uri: Uri = Uri.parse("package:$packageName")
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
                startActivityForResult(intent, PERMISS_REQUEST_CODE)
                return false
            }
        }
        return true
    }

    private fun checkPermission() {
        if (checkPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.REQUEST_INSTALL_PACKAGES))) {
            Log.e(TAG, "已获取存储网络权限")
            checkVersion()
        } else {
            Log.e(TAG, "请求存储网络权限")
            requestPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.REQUEST_INSTALL_PACKAGES), PERMISS_REQUEST_CODE)
        }
    }

    private fun checkVersion() {
        showLoadingView("检查版本...")
        mPresenter?.checkUpdate(this)
    }

    private fun downloadUpdate() {
        if (!connectState) {
            showToast("蓝牙腕表已断开连接")
            return
        }

        if (!App.isSynState()) {
            showToast("蓝牙腕表未同步完成")
            return
        }

        when (update_type) {
            UPDATE_WATCH -> {
                wirelessSend()
            }
            DOWNLOAD_FIRM -> {
                downloadDfu()
            }
        }
    }

    /**
     * 腕表版本校验  判断是否升级
     */
    private fun wirelessSend() {
        progesss_watch.progress = 0
        progesss_watch.max = 100
        showLoadingView("进行数据解压中...")
    }

    private fun downloadDfu() {
        state = DOWNLOADING
        tv_update_watch.isEnabled = false
        tv_update_watch.setTextColor(getColor(R.color.gray))
        tv_update_watch_status.text = "准备下载数据..."
        mPresenter?.downloadDfu(object : DownloadCallback {
            override fun onFinish(file: File?) {
                LogUtils.d("onFinish " + file!!.absolutePath)
                tv_update_watch_status.text = "下载完成"
                showToast("下载完成")
            }

            override fun onProgress(totalByte: Long, currentByte: Long, progress: Int) {
                if (totalByte == -1L && currentByte == -1L && progress == -3) {
                    Handler().postDelayed({ RxBus.getInstance().post("reconnet", "") }, 1000)
                    return
                } else if (currentByte == totalByte) {
                    tv_update_watch_status.text = "下载完成"
                    wirelessSend()
                    return
                } else {
                    tv_update_watch_status.text = "已下载数据  ${progress}%"
                }
                progesss_watch.progress = progress
            }

            override fun onError(msg: String?) {
                LogUtils.d("onError $msg")
            }

            override fun onStart(msg: Disposable?) {

            }
        })
    }

    /**
     * App版本校验 判断是否升级
     *
     */
    private fun updateApk() {
        state = DOWNLOADING
        tv_update_app.setTextColor(getColor(R.color.gray))
        tv_update_app_status.text = "准备下载数据..."
        tv_update_app.isEnabled = false
        mPresenter?.downloadApk(object : DownloadCallback {
            override fun onFinish(file: File?) {
                LogUtils.d("onFinish " + file!!.absolutePath)
                tv_update_app_status.text = "下载完成"
                showToast("下载完成")
            }

            override fun onProgress(totalByte: Long, currentByte: Long, progress: Int) {
                if (currentByte == totalByte) {
                    tv_update_app_status.text = "下载完成"
                    mPresenter?.installApk(this@UpdateFuncActivity)
                    state = COMPLETE
                } else {
                    tv_update_app_status.text = "已下载数据  ${progress}%"
                }
                progesss_app.progress = progress
            }

            override fun onError(msg: String?) {
                LogUtils.d("onError $msg")
            }

            override fun onStart(msg: Disposable?) {

            }
        })
    }

    override fun initListener() {
        im_back.setOnClickListener {
            if (!interceptFinish()) {
                this.finish()
            }
        }

        tv_update_app.setOnClickListener {
            if (state == DOWNLOADING || state == UPDATEFIRM) {
                showToast("正在下载升级")
                return@setOnClickListener
            }
            if (state == COMPLETE) {
                mPresenter?.installApk(this)
                return@setOnClickListener
            }
            if (checkInstall()) {
                updateApk()
            }
        }

        tv_update_watch.setOnClickListener {
            if (state == DOWNLOADING) {
                showToast("正在下载数据")
                return@setOnClickListener
            }
            downloadUpdate()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0x5555) {
                mPresenter?.installApk(this)
                state == FREE
                return
            } else if (requestCode == PERMISS_REQUEST_CODE) {
                updateApk()
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == 0x6666 || requestCode == 0x5555) {
                if (state == COMPLETE) {
                    val file = File(mPresenter?.getApkPaht())
                    if (file.exists()) {
                        tv_update_app.setTextColor(getColor(R.color.yellow))
                        tv_update_app.isEnabled = true
                        tv_update_app.text = "安装"
                    }
                    return
                }
            } else if (requestCode == PERMISS_REQUEST_CODE) {
                showToast("权限获取失败")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PERMISS_REQUEST_CODE == requestCode) {
            //未初始化就 先初始化一个用户对象
//            checkVersion()
        } else {
            showToast("权限申请失败")
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("onupdateprogress")])
    fun onUpdateProgress(progress: String) {
        val value = progress.toInt()
        if (value >= 0) {
            cancelLoadingView()
            progesss_watch.progress = value
            tv_update_watch_status.text = "已升级腕表  $value%"
            if (value == 100) {
                if (tv_update_app.text == "安装") {
                    state = COMPLETE
                } else {
                    state = FREE
                }
                mPresenter?.updateDate()
                Handler().postDelayed({
                    setResult(Activity.RESULT_OK)
                    this.finish()
                }, 2000)
                connectState = false
                App.setSynState(false)
                firm_update_time = sdf.format(System.currentTimeMillis())
                showToast("腕表升级完成,退出升级功能")
            }
        } else if (value == -1) {
            showLoadingView("准备腕表升级...")
            state == UPDATEFIRM
        } else if (value == -2) {
            progesss_watch.progress = 0
            state = FREE
            App.setSynState(false)
            connectState = false
            tv_update_watch_status.text = "等待下载"
            tv_update_watch.isEnabled = true
            tv_update_watch.setTextColor(getColor(R.color.blue_light))
            cancelLoadingView()
            showToast("升级失败，请稍后重试")
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag("onupdatestatus")])
    fun onUpdateStatus(status: String) {
        this.status = status
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingView?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        loadingView = null
        status = null
    }

    override fun getMainPresent() = MainUpdatePresenter()
    override fun updateApkState(state: Boolean, versionName: String) {
//        tv_app_version.text = "JIANGTAI $versionName
        tv_app_version.setTextColor(getColor(if (state) R.color.blue_light else R.color.gray))
        if (state) {
            tv_app_update_date.visibility = View.GONE
            progesss_app.visibility = View.VISIBLE

            tv_update_app_status.visibility = View.VISIBLE
            tv_update_app_status.text = "等待下载"
            tv_update_app.visibility = View.VISIBLE
        } else {
            tv_app_update_date.visibility = View.VISIBLE
            tv_app_update_date.text = apk_update_time

            progesss_app.visibility = View.GONE
            tv_update_app_status.visibility = View.GONE
            tv_update_app.visibility = View.GONE
        }
        cancelLoadingView()
    }

    override fun updateFirmState(apolloState: Boolean, apolloVersion: String, bleState: Boolean, bleVersion: String) {
        tv_apollo_version.text = "Apollo $apolloVersion"
        tv_ble_version.text = "Ble $bleVersion"
        tv_apollo_version.setTextColor(getColor(if (apolloState) R.color.blue_light else R.color.gray))
        tv_ble_version.setTextColor(getColor(if (bleState) R.color.blue_light else R.color.gray))

        if (apolloState || bleState) {
            tv_update_watch_status.visibility = View.VISIBLE
            tv_update_watch_status.text = "等待下载"
            tv_watch_update_date.visibility = View.GONE
            progesss_watch.visibility = View.VISIBLE
            tv_update_watch.visibility = View.VISIBLE
            tv_update_watch.text = "下载固件"
            update_type = DOWNLOAD_FIRM
        } else {
            tv_update_watch_status.visibility = View.GONE
            tv_watch_update_date.visibility = View.VISIBLE
            tv_watch_update_date.text = firm_update_time

            progesss_watch.visibility = View.GONE
            tv_update_watch.visibility = View.GONE
        }
    }

    override fun initWatchUI(apolloVersion: String, bleVersion: String) {
        tv_apollo_version.text = "Apollo $apolloVersion"
        tv_ble_version.text = "Ble $bleVersion"
        if (TextUtils.isEmpty(apolloVersion) && TextUtils.isEmpty(bleVersion)) {
            tv_watch_update_date.text = "无连接"
        } else {
            tv_watch_update_date.text = firm_update_time
        }

    }

    override fun complete() {
        apk_update_time = sdf.format(System.currentTimeMillis())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && interceptFinish()) {
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun interceptFinish(): Boolean {
        if (state == DOWNLOADING || state == UPDATEFIRM) {
            showToast("正在下载升级")
            return true
        } else if (state == FREE) {
            return false
        } else if (state == COMPLETE) {
            return false
        }
        return false
    }

    override fun reflesh() {

    }
}