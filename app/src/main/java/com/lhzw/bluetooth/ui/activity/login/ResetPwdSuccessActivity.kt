package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_reset_pwd_success.*

/**
 * Created by heCunCun on 2020/9/21
 */
class ResetPwdSuccessActivity:BaseActivity() {
    override fun attachLayoutRes(): Int= R.layout.activity_reset_pwd_success

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        btn_next.setOnClickListener {
            Intent(this,LoginActivity::class.java).apply {
                startActivity(this)
            }
        }
    }
}