package com.lhzw.bluetooth.ui.activity

import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_webview.*

/**
 * Created by heCunCun on 2020/10/23
 */
class UserAgreementActivity:BaseActivity() {
    override fun attachLayoutRes(): Int= R.layout.activity_user_agreement

    override fun initData() {

    }

    override fun initView() {
        tv_title.text="用户协议和隐私政策"
    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
    }
}