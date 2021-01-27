package com.lhzw.bluetooth.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.net.BaseBean
import com.lhzw.bluetooth.bean.net.SubJoin
import com.lhzw.bluetooth.bean.net.UserInfo
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.net.CallbackListObserver
import com.lhzw.bluetooth.net.SLMRetrofit
import com.lhzw.bluetooth.net.ThreadSwitchTransformer
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.widget.LoadingView
import kotlinx.android.synthetic.main.activity_login.*
import java.text.SimpleDateFormat

/**
 *
 *    已弃用
 * Date： 2020/6/2 0002
 * Time： 10:31
 * Created by xtqb.
 */
class LoginActivity : AppCompatActivity() {
    private val TAG = "Login"
    private var mGlobalToast: Toast? = null
    private var http_token: String? by Preference(Constants.HTTP_TOOKEN, "")
    private var apk_update_time: String? by Preference(Constants.APK_UPDATE_TIME, "")


    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setContentView(R.layout.activity_login)

        if ("" != http_token) {
            jumpToMain()
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//使activity都竖屏
            initView()
            initData()
            setLisetener()
        }
    }

    private fun setLisetener() {
        tv_login.setOnClickListener {
            if (it.id == R.id.tv_login) {
                if (et_account.text == null || et_account.text.toString().isEmpty() || et_password.text == null || et_password.text.toString().isEmpty()) {
                    showToast("请检查登录信息是否为空！")
                    return@setOnClickListener
                }
                login()
//                jumpToMain()
            }
        }
    }

    private var loadingView: LoadingView? = null
    private fun login() {
        if (loadingView == null) {
            loadingView = LoadingView(this@LoginActivity)

        }
        loadingView?.setLoadingTitle("登录中...")
        loadingView?.show()
        val response = SLMRetrofit.getInstance().getApi()?.login(et_account.text.toString(), et_password.text.toString())
        response?.compose(ThreadSwitchTransformer())?.subscribe(object : CallbackListObserver<BaseBean<UserInfo<SubJoin>>?>() {
            override fun onSucceed(bean: BaseBean<UserInfo<SubJoin>>?) {
                bean?.let {
                    if (it.isSuccessed()) {
                        http_token = it.getData()?.getToken()
                        showToast("登录成功")
                        if ("" == apk_update_time) {// 说明第一次登陆软件
                            val sdf = SimpleDateFormat("yyyy年MM月dd日更新")
                            apk_update_time = sdf.format(System.currentTimeMillis())
                        }
                        jumpToMain()
                    } else {
                        showToast("${it.getMessage()}")
                        Log.e(TAG, "code ${it.getCode()}  message  ${it.getMessage()}")
                    }
                    if (loadingView != null && loadingView!!.isShowing) {
                        loadingView?.cancel()
                    }
                }
            }

            override fun onFailed() {
                Log.e(TAG, "登录失败")
                showToast("登录失败")
                if (loadingView != null && loadingView!!.isShowing) {
                    loadingView?.cancel()
                }
            }
        })
    }

    private fun jumpToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


    private fun initData() {
        // 初始化进度条
    }

    private fun initView() {

    }

    fun showToast(text: String?) {
        if (mGlobalToast == null) {
            mGlobalToast = Toast.makeText(App.context, text, Toast.LENGTH_SHORT)
            mGlobalToast?.show()
        } else {
            mGlobalToast!!.setText(text)
            mGlobalToast!!.duration = Toast.LENGTH_SHORT
            mGlobalToast!!.show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mGlobalToast = null
    }
}

