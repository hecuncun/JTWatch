package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.event.CloseEvent
import kotlinx.android.synthetic.main.activity_set_step_and_cal_target.*
import kotlinx.android.synthetic.main.activity_set_weight.btn_next
import kotlinx.android.synthetic.main.activity_set_weight.iv_back
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.extension.findAll

/**
 * Created by heCunCun on 2020/8/14
 */
class SetStepAndCalTargetActivity:BaseActivity() {

    private var personalInfoBean: PersonalInfoBean? = null//个人信息
    override fun attachLayoutRes(): Int = R.layout.activity_set_step_and_cal_target

    override fun initData() {
        val list = LitePal.findAll<PersonalInfoBean>()
        personalInfoBean = list[0]
        iv_sex.setImageResource(if (personalInfoBean!!.gender==1) R.mipmap.ic_man else R.mipmap.ic_women)
    }

    override fun initView() {}

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        hv_step.setOnItemChangedListener { index, value ->
            tv_step.text=value.toString()
        }

        hv_cal.setOnItemChangedListener { index, value ->
            tv_cal.text=value.toString()
        }
        btn_next.setOnClickListener {
            personalInfoBean!!.des_steps=tv_step.text.toString().toInt()
            personalInfoBean!!.des_calorie=tv_cal.text.toString().toInt()
            personalInfoBean!!.save()
            Intent(this,SetHeightActivity::class.java).apply {
                startActivity(this)
            }
        }
    }
    override fun useEventBus(): Boolean =true
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun finishEvent(eventBus:CloseEvent){
          finish()
    }
}