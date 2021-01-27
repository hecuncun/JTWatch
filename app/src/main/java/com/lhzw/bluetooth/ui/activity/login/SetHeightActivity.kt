package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.event.CloseEvent
import com.lhzw.bluetooth.ui.activity.MainActivity
import kotlinx.android.synthetic.main.activity_set_height.*
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import org.litepal.extension.findAll

/**
 * Created by heCunCun on 2020/8/14
 */
class SetHeightActivity : BaseActivity() {
    private var personalInfoBean: PersonalInfoBean? = null//个人信息
    override fun attachLayoutRes(): Int = R.layout.activity_set_height

    override fun initData() {
        val list = LitePal.findAll<PersonalInfoBean>()
        personalInfoBean = list[0]
        iv_sex.setImageResource(if (personalInfoBean!!.gender == 1) R.mipmap.ic_man else R.mipmap.ic_women)
    }

    override fun initView() {}

    override fun initListener() {
        hv_height.setOnItemChangedListener { index, value ->
            tv_height.text = value.toString()
        }

        iv_back.setOnClickListener {
            finish()
        }

        btn_next.setOnClickListener {
            personalInfoBean!!.height = tv_height.text.toString().toInt()
            personalInfoBean!!.save()
            Intent(this, MainActivity::class.java).apply {
                startActivity(this)
                //销毁以前所有的Activity
                EventBus.getDefault().post(CloseEvent())
                finish()
            }
        }
    }
}