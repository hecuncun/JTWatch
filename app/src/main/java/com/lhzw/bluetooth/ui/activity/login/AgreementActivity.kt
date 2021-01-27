package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.ui.activity.MainActivity
import kotlinx.android.synthetic.main.activity_agreement.*

/**
 * Created by heCunCun on 2020/8/12
 */
class AgreementActivity:BaseActivity() {

    override fun attachLayoutRes(): Int=R.layout.activity_agreement

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
       iv_close.setOnClickListener {
           isAgreement=false
           finish()
       }
        btn_ok.setOnClickListener {
            Intent(this,LoginActivity::class.java).apply {
                isAgreement=true
                startActivity(this)
            }
        }

    }
}