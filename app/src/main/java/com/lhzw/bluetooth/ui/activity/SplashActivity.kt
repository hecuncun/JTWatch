package com.lhzw.bluetooth.ui.activity

import android.content.Intent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.dialog.AgreementDialog
import com.lhzw.bluetooth.ui.activity.login.LoginActivity
import com.lhzw.bluetooth.uitls.Preference
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Created by hecuncun on 2019/11/12
 */
class SplashActivity : BaseActivity() {
    private var alphaAnimation: AlphaAnimation? = null

    override fun attachLayoutRes(): Int = R.layout.activity_splash
    override fun initData() {
    }
    private var isAgree: Boolean by Preference(Constants.IS_AGREE, false)
    private var agreementDialog:AgreementDialog?=null
    override fun initView() {

        if (firest_login) {
            //显示权限弹框
            agreementDialog= AgreementDialog(this)
            agreementDialog?.setCanceledOnTouchOutside(false)
            agreementDialog?.setCancelable(false)
            if (!isAgree){//还未同意
                agreementDialog?.show()
                // 适配今日头条弹窗不居中解决
//            val lp= agreementDialog!!.window.attributes;
////设置宽高，高度默认是自适应的，宽度根据屏幕宽度比例设置
//            lp.width = ScreenUtils.getWidth(this);
////这里设置居中
//            lp.gravity = Gravity.CENTER;
//            agreementDialog?.window?.attributes = lp
                agreementDialog?.setOnConfirmListener(View.OnClickListener {
                    //不同意
                    agreementDialog?.dismiss()
                    finish()
                })
            }
            fadeIn(tv_login, 0.1f, 1.0f, 2300)
            tv_login.setOnClickListener {
                firest_login = false
                jumpToLogin()
            }
        }
        alphaAnimation = AlphaAnimation(0.3F, 1.0F)
        alphaAnimation?.run {
            duration = 2000
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    if (!firest_login) {
                        jumpToLogin()

                    }
                }

                override fun onAnimationStart(p0: Animation?) {
                }
            })
        }
        splash_view.startAnimation(alphaAnimation)
    }

    private fun jumpToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun initListener() {
    }

    private fun fadeIn(view: View, startAlpha: Float, endAlpha: Float, duration: Long) {
        if (view.visibility == View.VISIBLE) return
        view.visibility = View.VISIBLE
        val animation: Animation = AlphaAnimation(startAlpha, endAlpha)
        animation.duration = duration
        view.startAnimation(animation)
    }

    private fun fadeIn(view: View) {
        fadeIn(view, 0f, 1f, 400)
        view.isEnabled = true
    }

    private fun fadeOut(view: View) {
        if (view.visibility != View.VISIBLE) return
        view.isEnabled = false
        val animation: Animation = AlphaAnimation(1f, 0f)
        animation.duration = 400
        view.startAnimation(animation)
        view.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        alphaAnimation = null
    }

}