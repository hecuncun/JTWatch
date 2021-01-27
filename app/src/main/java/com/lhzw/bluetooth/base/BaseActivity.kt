package com.lhzw.bluetooth.base

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.uitls.CommonUtil
import com.lhzw.bluetooth.uitls.KeyBoardUtil
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.uitls.StatusBarUtil
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by hecuncun on 2019/11/11
 */
abstract class BaseActivity : AppCompatActivity() {
    protected var connectState: Boolean by Preference(Constants.CONNECT_STATE, false)
    protected var firest_login: Boolean by Preference(Constants.FIREST_LOGIN, true)
    protected var photoPath: String? by Preference(Constants.PHOTO_PATH, "")

    protected var http_token: String? by Preference(Constants.HTTP_TOOKEN, "")
    protected var cachePhone: String? by Preference(Constants.HTTP_CACHE_TOOKEN, "")
    protected var nickName: String by Preference(Constants.NICK_NAME, "")

    protected var isAgreement:Boolean by Preference(Constants.IS_AGREEMENT,false)

    /**
     * 布局文件id
     */
    protected abstract fun attachLayoutRes(): Int

    /**
     * 初始化数据
     */
    abstract fun initData()

    /**
     * 初始化 View
     */
    abstract fun initView()

    /**
     * 初始化监听器
     */
    abstract fun initListener()

    /**
     * 是否使用 EventBus
     */
    open fun useEventBus(): Boolean = false


    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//使activity都竖屏
        setContentView(attachLayoutRes())
        if (useEventBus()) {
            EventBus.getDefault().register(this)
        }
        //initToolBar()
        initView()

        initListener()
        initData()
        initStateBarColor()

    }

    override fun onResume() {
        super.onResume()
        App.setActivityContext(this@BaseActivity)
    }

//    private fun initToolBar() {
//        if (toolbar != null) {
//            toolbar.setNavigationIcon(R.mipmap.icon_return)
//            toolbar.title = ""
//            setSupportActionBar(toolbar)
//            toolbar.setNavigationOnClickListener(View.OnClickListener {
//                finish()
//                KeyboardUtils.hideSoftInput(this@BaseActivity)
//            })
//        }
//    }

    open fun initStateBarColor() {
        val mThemeColor = App.context.resources.getColor(R.color.colorPrimary)//设置状态栏颜色
        StatusBarUtil.setColor(this, mThemeColor, 0)
        if (this.supportActionBar != null) {
            this.supportActionBar?.setBackgroundDrawable(ColorDrawable(mThemeColor))
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) {
            val v = currentFocus
            // 如果不是落在EditText区域，则需要关闭输入法
            if (KeyBoardUtil.isHideKeyboard(v, ev)) {
                KeyBoardUtil.hideKeyBoard(this, v)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Fragment 逐个出栈
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onPause() {
        super.onPause()
        App.setActivityContext(null)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (useEventBus()) {
            EventBus.getDefault().unregister(this)
        }
        CommonUtil.fixInputMethodManagerLeak(this)
        App.getRefWatcher(this)?.watch(this)//开始检测内存泄漏
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
            ActivityCompat.requestPermissions(this, needPermissions.toTypedArray(), REQUEST_CODE_PERMISSION)
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
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e("permission", "$permission 没授权")
//                showToast("$permission 还没授权")
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
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED || ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
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
        AlertDialog.Builder(this)
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
    fun permissionFail(requestCode: Int) {
        Log.d(TAG, "获取权限失败=$requestCode")
    }

    /**
     * 启动当前应用设置页面
     */
    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    /**
     * 获取权限成功
     *
     * @param requestCode
     */
    fun permissionSuccess(requestCode: Int) {
        Log.d(TAG, "获取权限成功=$requestCode")
    }

}