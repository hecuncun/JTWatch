package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.*
import com.lhzw.bluetooth.bean.net.BaseBean
import com.lhzw.bluetooth.bean.net.SubJoin
import com.lhzw.bluetooth.bean.net.UserInfo
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.dialog.AgreementDialog
import com.lhzw.bluetooth.event.CloseEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.net.CallbackListObserver
import com.lhzw.bluetooth.net.SLMRetrofit
import com.lhzw.bluetooth.net.ThreadSwitchTransformer
import com.lhzw.bluetooth.ui.activity.MainActivity
import com.lhzw.bluetooth.ui.activity.UserAgreementActivity
import com.lhzw.bluetooth.ui.activity.web.WebViewActivity
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.widget.LoadingView
import kotlinx.android.synthetic.main.activity_login_2.*
import kotlinx.android.synthetic.main.activity_login_2.checkbox
import kotlinx.android.synthetic.main.activity_login_2.et_pwd
import kotlinx.android.synthetic.main.activity_login_2.iv_eye
import kotlinx.android.synthetic.main.activity_login_2.tv_register
import kotlinx.android.synthetic.main.activity_login_new.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat

/**
 * Created by heCunCun on 2020/8/7
 *  新的登录页UI
 */
class LoginActivity : BaseActivity() {
    private var apk_update_time: String? by Preference(Constants.APK_UPDATE_TIME, "")
    private var apk_ip_change: Boolean? by Preference(Constants.APK_IP_CHANGE, false)
    private var registerTime: Long? by Preference(Constants.REGISTERTIME, 0)

    override fun attachLayoutRes(): Int = R.layout.activity_login_2

    override fun initData() {
        if (!apk_ip_change!! && packageManager.getPackageInfo(packageName, 0).versionName == "v3.2.3") {
            http_token = ""
            apk_ip_change = true
            return
        }
        if ("" != http_token) {
            jumpToMain()
        }

        et_phone.setText(cachePhone)//默认填写注册账号  或者上次登录账号
    }

    override fun initView() {


        tv_register.setOnClickListener {
            Intent(this, RegisterActivity::class.java).apply {
                startActivity(this)
            }
        }
        desc.setOnClickListener {
//            Intent(this,UserAgreementActivity::class.java).apply {
//                startActivity(this)
//            }
            val intent = Intent(App.context, WebViewActivity::class.java)
            intent.putExtra("url","http://www.cetcjt.com/ysxy")
            startActivity(intent)
        }
        btn_login.setOnClickListener {
            val phone = et_phone.text.toString().trim()
            val pwd = et_pwd.text.toString().trim()
            if (phone.isEmpty()) {
                showToast("请检查手机号码是否正确")
                return@setOnClickListener
            }
            if (pwd.isEmpty()) {
                showToast("请输入密码")
                return@setOnClickListener
            }
            //走登陆接口
            if (checkbox.isChecked) {
                login(phone, pwd)
            } else {
                showToast("请先勾选已阅读并同意《疆泰用户协议和隐私政策》")
            }

        }
    }

    override fun initListener() {
        iv_clear.setOnClickListener {
            et_pwd.setText("")
        }
        iv_eye.setOnClickListener {
            switchPwdMode(et_pwd, iv_eye)
        }
        tv_reset_pwd.setOnClickListener {
            Intent(this,ResetPwdActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    /**
     * 显示密码操作
     */
    private fun switchPwdMode(
            etPwd: EditText,
            btnPwdEye: ImageView
    ) {
        if (etPwd.text.toString().isEmpty()) {
            return
        }
        //是否已经显示了
        val showPwd = etPwd.transformationMethod != PasswordTransformationMethod.getInstance()
        if (showPwd) {
            //否则隐藏密码、
            etPwd.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnPwdEye.setImageResource(R.drawable.ic_login_eye_close)
            etPwd.transformationMethod = PasswordTransformationMethod.getInstance()
            //光标最后
            etPwd.setSelection(etPwd.text.toString().length)
        } else {
            btnPwdEye.setImageResource(R.drawable.ic_login_eye_open)
            etPwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
            //光标最后
            etPwd.setSelection(etPwd.text.toString().length)
        }
    }

    private var loadingView: LoadingView? = null
    private var setTarget = false
    private fun login(phone: String, pwd: String) {
        if (loadingView == null) {
            loadingView = LoadingView(this)
        }
        loadingView?.setLoadingTitle("登录中...")
        loadingView?.show()
        val response = SLMRetrofit.getInstance().getApi()?.login(phone, pwd)
        response?.compose(ThreadSwitchTransformer<BaseBean<UserInfo<SubJoin>>>())?.subscribe(object : CallbackListObserver<BaseBean<UserInfo<SubJoin>>?>() {
            override fun onSucceed(bean: BaseBean<UserInfo<SubJoin>>?) {
                bean?.let {
                    if (it.isSuccessed()) {
                        if (cachePhone != phone) {
                            setTarget = true
                            if (cachePhone != "") {
                                deleteAllSport()
                            }
                        }
                        cachePhone = phone
                        http_token = it.getData()?.getToken()

                        Log.e("LOGINTIME", "时间   :   ${it.getData()?.getInfo()?.getCreateTime()}")
                        registerTime = DateUtils.dateToLong(it.getData()?.getInfo()?.getCreateTime())
                        showToast("登录成功")
                        if (nickName.isEmpty()) {//默认用户名为登录名
                            nickName = phone
                        }
                        if ("" == apk_update_time) {// 说明第一次登陆软件
                            val sdf = SimpleDateFormat("yyyy年MM月dd日更新")
                            apk_update_time = sdf.format(System.currentTimeMillis())
                        }
                        if (setTarget) {
                            //新用户重新设置目标
                            Intent(this@LoginActivity, SetNickNameActivity::class.java).apply {
                                startActivity(this)
                            }
                        } else {
                            //老用户直接入主页
                            jumpToMain()
                        }
                    } else {
                        showToast("${it.getMessage()}")

                    }
                    if (loadingView != null && loadingView!!.isShowing) {
                        loadingView?.cancel()
                    }
                }
            }

            override fun onFailed() {
                showToast("登录失败")
                if (loadingView != null && loadingView!!.isShowing) {
                    loadingView?.cancel()
                }
            }
        })
    }

    /**
     * 更改 用户删除基础数据
     */
    private fun deleteAllSport() {
        CommOperation.deleteAll(WatchInfoBean::class.java)
        CommOperation.deleteAll(BoundaryAdrrBean::class.java)
        CommOperation.deleteAll(ClimbingSportBean::class.java)
        CommOperation.deleteAll(CurrentDataBean::class.java)
        CommOperation.deleteAll(DailyDataBean::class.java)
        CommOperation.deleteAll(DailyInfoDataBean::class.java)
        CommOperation.deleteAll(FlatSportBean::class.java)
        CommOperation.deleteAll(SportActivityBean::class.java)
        CommOperation.deleteAll(SportInfoAddrBean::class.java)
    }

    private fun jumpToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun useEventBus(): Boolean = true

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun finishEvent(eventBus: CloseEvent) {
        finish()
    }
}