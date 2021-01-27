package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.MsgVerifyBean
import com.lhzw.bluetooth.bean.net.BaseBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.net.CallbackListObserver
import com.lhzw.bluetooth.net.SLMRetrofit
import com.lhzw.bluetooth.net.ThreadSwitchTransformer
import com.lhzw.bluetooth.uitls.CountDownTimerUtils
import com.lhzw.bluetooth.uitls.RegexUtil
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_reset_pwd.*

/**
 * Created by heCunCun on 2020/9/21
 */
class ResetPwdActivity : BaseActivity() {
    private var countDownTimerUtils: CountDownTimerUtils? = null

    override fun attachLayoutRes(): Int = R.layout.activity_reset_pwd

    override fun initData() {

    }

    override fun initView() {
        countDownTimerUtils = CountDownTimerUtils(tv_get_code, 60000, 1000)//TextView计时器
    }

    override fun initListener() {
        tv_get_code.setOnClickListener {
            val phone = et_phone.text.toString().trim()
            if (RegexUtil.checkMobile(phone)) {
                //获取验证码
                val msgVerifyCode = SLMRetrofit.getInstance().getApi()?.getMsgVerifyCode(MsgVerifyBean(phone, phone, "", "", Constants.VERIFY_TYPE_MODIFY_PASSWORD))
                msgVerifyCode?.compose(ThreadSwitchTransformer())?.subscribe(object : CallbackListObserver<BaseBean<String>>() {
                    override fun onSucceed(t: BaseBean<String>) {
                        if (t.isSuccessed()) {
                            showToast("获取短信验证码成功")
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
        tv_cancel.setOnClickListener {
            finish()
        }
        iv_close.setOnClickListener {
            finish()
        }

        btn_next.setOnClickListener {
            val phone = et_phone.text.toString().trim()
            val code = et_code.text.toString().trim()
            val pwd = et_pwd.text.toString().trim()
            val pwd2 = et_pwd2.text.toString().trim()
            if (phone.isEmpty()) {
                showToast("请输入手机号")
                return@setOnClickListener
            }
            if (code.isEmpty()){
                showToast("请输入短信验证码")
                return@setOnClickListener
            }
            if (pwd.length in 6..20){
                if (pwd != pwd2){
                    showToast("两次输入密码不一致")
                    return@setOnClickListener
                    }
            }else{
                showToast("密码长度须在6-20位之间")
                return@setOnClickListener
            }

            val modifyPassword = SLMRetrofit.getInstance().getApi()?.modifyPassword(MsgVerifyBean(phone, phone, code, pwd, Constants.VERIFY_TYPE_MODIFY_PASSWORD))

            modifyPassword?.compose(ThreadSwitchTransformer())?.subscribe(object :CallbackListObserver<BaseBean<String>>(){
                override fun onSucceed(t: BaseBean<String>) {
                    if (t.isSuccessed()){
                        //成功
                        Intent(this@ResetPwdActivity,ResetPwdSuccessActivity::class.java).apply {
                            startActivity(this)
                            finish()
                        }
                    }else{
                        showToast(t.getMessage().toString())
                    }
                }

                override fun onFailed() {
                }
            })
        }
    }
}