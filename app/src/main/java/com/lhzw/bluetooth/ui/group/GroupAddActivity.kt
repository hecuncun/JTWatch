package com.lhzw.bluetooth.ui.group

import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_group_add.*

/**
 * Created by heCunCun on 2021/2/22
 */
class GroupAddActivity:BaseActivity() {
    override fun attachLayoutRes(): Int = R.layout.activity_group_add

    override fun initData() {
    }

    override fun initView() {
    }

    override fun initListener() {
        iv_add.setOnClickListener {
            //请求接口创建群组
            //
            finish()
        }
    }
}