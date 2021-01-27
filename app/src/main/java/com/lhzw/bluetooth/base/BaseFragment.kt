package com.lhzw.bluetooth.base

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.LayoutRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.uitls.Preference
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by hecuncun on 2019/11/12
 */
abstract class BaseFragment :Fragment(){
    /**
     * 手表是否已连接
     */
    protected var connectState: Boolean by Preference(Constants.CONNECT_STATE, false)
    protected var photoPath: String? by Preference(Constants.PHOTO_PATH, "")
    protected var nickName: String by Preference(Constants.NICK_NAME, "")
    protected var http_token: String? by Preference(Constants.HTTP_TOOKEN, "")
    /**
     * 视图是否加载完毕
     */
    private var isViewPrepare = false
    /**
     * 数据是否加载过了
     */
    protected var hasLoadData = false


    protected var mContext : Context? = null

    /**
     * 加载布局
     */
    @LayoutRes
    abstract fun attachLayoutRes(): Int

    /**
     * 初始化 View
     */
    abstract fun initView(view: View)
    /**
     * 懒加载
     */
    abstract fun lazyLoad()
    /**
     * 是否使用 EventBus
     */
    open fun useEventBus(): Boolean = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(attachLayoutRes(),null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(useEventBus()){
            EventBus.getDefault().register(this)
        }

        isViewPrepare = true
        initView(view)

        lazyLoadDataIfPrepared()
    }

    private fun lazyLoadDataIfPrepared() {
        if (userVisibleHint && isViewPrepare && !hasLoadData){
            lazyLoad()
            hasLoadData = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(useEventBus()){
            EventBus.getDefault().unregister(this)
        }
        activity?.let { App.getRefWatcher(it)?.watch(activity) }
    }


    //权限相关
    private val TAG = "PermissionsUtil"
    private var REQUEST_CODE_PERMISSION = 0x00099
    /**
     * 请求权限
     *
     *
     * 警告：此处除了用户拒绝外，唯一可能出现无法获取权限或失败的情况是在AndroidManifest.xml中未声明权限信息
     * Android6.0+即便需要动态请求权限（重点）但不代表着不需要在AndroidManifest.xml中进行声明。
     *
     * @param permissions 请求的权限
     * @param requestCode 请求权限的请求码
     */
    fun requestPermission(permissions: Array<String>, requestCode: Int) {
        this.REQUEST_CODE_PERMISSION = requestCode
        if (checkPermissions(permissions)) {
            permissionSuccess(REQUEST_CODE_PERMISSION)
        } else {
            val needPermissions = getDeniedPermissions(permissions)
            requestPermissions(needPermissions.toTypedArray(), REQUEST_CODE_PERMISSION)//使用fragment的此方法,可以拿到回调
        }
    }

    /**
     * 检测所有的权限是否都已授权
     *
     * @param permissions
     * @return
     */
    fun checkPermissions(permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     */
    private fun getDeniedPermissions(permissions: Array<String>): List<String> {
        val needRequestPermissionList = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED || ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)) {
                needRequestPermissionList.add(permission)
            }
        }
        return needRequestPermissionList
    }


    /**
     * 系统请求权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (verifyPermissions(grantResults)) {
                permissionSuccess(REQUEST_CODE_PERMISSION)
            } else {
                permissionFail(REQUEST_CODE_PERMISSION)
                showTipsDialog()
            }
        }
    }

    /**
     * 确认所有的权限是否都已授权
     *
     * @param grantResults
     * @return
     */
    private fun verifyPermissions(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * 显示提示对话框
     */
    fun showTipsDialog() {
        AlertDialog.Builder(activity!!)
                .setTitle("警告")
                .setMessage("需要必要的权限才可以正常使用该功能，您已拒绝获得该权限。\n" +
                        "如果需要重新授权，您可以点击“确定”按钮进入系统设置进行授权")
                .setNegativeButton("取消") { dialog, i ->
                    dialog.dismiss()
                }
                .setPositiveButton("确定") { dialogInterface, i ->
                    startAppSettings()
                }
                .show()
    }

    /**
     * 权限获取失败
     *
     * @param requestCode
     */
    open fun permissionFail(requestCode: Int) {
        Log.d(TAG, "获取权限失败=$requestCode")
    }

    /**
     * 启动当前应用设置页面
     */
    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${activity?.packageName}")
        startActivity(intent)
    }

    /**
     * 获取权限成功
     *
     * @param requestCode
     */
    open fun permissionSuccess(requestCode: Int) {
        Log.d(TAG, "获取权限成功=$requestCode")
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }
}