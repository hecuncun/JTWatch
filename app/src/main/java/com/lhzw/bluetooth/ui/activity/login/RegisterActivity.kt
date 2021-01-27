package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.MsgVerifyBean
import com.lhzw.bluetooth.bean.net.BaseBean
import com.lhzw.bluetooth.bean.net.LoginUser
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.net.CallbackListObserver
import com.lhzw.bluetooth.net.SLMRetrofit
import com.lhzw.bluetooth.net.ThreadSwitchTransformer
import com.lhzw.bluetooth.ui.activity.UserAgreementActivity
import com.lhzw.bluetooth.ui.activity.web.WebViewActivity
import com.lhzw.bluetooth.uitls.CountDownTimerUtils
import com.lhzw.bluetooth.uitls.RegexUtil
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_login_2.*
import kotlinx.android.synthetic.main.activity_register_2.*
import kotlinx.android.synthetic.main.activity_register_2.et_phone
import kotlinx.android.synthetic.main.activity_register_2.et_pwd
import kotlinx.android.synthetic.main.activity_register_2.iv_eye

/**
 * Created by heCunCun on 2020/8/12
 */
class RegisterActivity : BaseActivity() {
    private var countDownTimerUtils: CountDownTimerUtils? = null

    override fun attachLayoutRes(): Int = R.layout.activity_register_2

    override fun initData() {

    }

    override fun initView() {
        countDownTimerUtils = CountDownTimerUtils(tv_get_code, 60000, 1000)//TextView计时器
    }

    override fun initListener() {
        iv_back.setOnClickListener { finish() }
        tv_get_code.setOnClickListener {
            val phone = et_phone.text.toString().trim()
            if (RegexUtil.checkMobile(phone)) {
                //获取验证码
                val msgVerifyCode = SLMRetrofit.getInstance().getApi()?.getMsgVerifyCode(MsgVerifyBean(phone, phone,"","", Constants.VERIFY_TYPE_REGISTER))
                msgVerifyCode?.compose(ThreadSwitchTransformer())?.subscribe(object : CallbackListObserver<BaseBean<String>>() {
                    override fun onSucceed(t: BaseBean<String>) {
                        if (t.isSuccessed()) {
                            showToast("获取验证码成功")
                            Logger.e("验证码==${t.getData()}")
                            countDownTimerUtils?.start()
                        } else {
                            showToast(t.getMessage().toString())
                        }

                    }

                    override fun onFailed() {

                    }
                })
            } else {
                showToast("请检查手机号码是否正确")
            }
        }

        btn_register.setOnClickListener {//注册
            val phone = et_phone.text.toString().trim()
            val pwd = et_pwd.text.toString().trim()
            if (pwd.length in 6..20) {
                //走注册接口
                val loginBean = LoginUser(phone, "", 0, pwd, phone, "")
                val insertUserCall = SLMRetrofit.getInstance().getApi()?.insertUser(loginBean,et_code.text.toString().trim())
                insertUserCall?.compose(ThreadSwitchTransformer())?.subscribe(object : CallbackListObserver<BaseBean<String>>() {
                    override fun onSucceed(t: BaseBean<String>) {
                        if (t.isSuccessed()) {
                            showToast("注册成功")
                            cachePhone=phone
                            finish()
                        } else if (t.getCode() == "-1") {
                            showToast(t.getMessage() + "")
                            Logger.e(t.getMessage()+"")
                        }else{
                            Logger.e(t.getMessage()+"")
                        }
                    }

                    override fun onFailed() {

                    }
                })
            } else {
                showToast("密码长度须在6-20位之间")
            }
        }

        tv_login.setOnClickListener {
            Intent(this@RegisterActivity,LoginActivity::class.java).apply {
                startActivity(this)
            }
        }
        iv_eye.setOnClickListener {
            switchPwdMode(et_pwd, iv_eye)
        }
        ll_private.setOnClickListener {
            //点击隐私
//            val intent = Intent(App.context, WebViewActivity::class.java)
//            intent.putExtra("url","http://www.cetcjt.com/ysxy")
//            startActivity(intent)
            Intent(this, UserAgreementActivity::class.java).apply {
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

}