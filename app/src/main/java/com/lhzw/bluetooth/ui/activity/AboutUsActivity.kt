package com.lhzw.bluetooth.ui.activity

import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_about_us.*


/**
 * Date： 2020/6/18 0018
 * Time： 10:26
 * Created by xtqb.
 */

class AboutUsActivity : BaseActivity() {
    override fun attachLayoutRes() = R.layout.activity_about_us

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initListener() {
        im_back.setOnClickListener{
            this.finish()
        }
    }

}