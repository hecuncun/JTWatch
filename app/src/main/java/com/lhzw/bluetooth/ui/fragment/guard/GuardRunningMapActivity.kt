package com.lhzw.bluetooth.ui.fragment.guard

import android.os.CountDownTimer
import android.view.View
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.RefreshGuardState
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.Preference
import kotlinx.android.synthetic.main.activity_guard_running_map.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by heCunCun on 2021/2/6
 */
class GuardRunningMapActivity:BaseActivity() {
    private var guardEndTime: Long by Preference(Constants.GUARD_END_TIME, 0)//守护开启状态
    private var countDownTimer:CountDownTimer?=null
    override fun attachLayoutRes(): Int= R.layout.activity_guard_running_map

    override fun initData() {
        refreshGuardState()
    }

    override fun initView() {

    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        iv_finish.setOnClickListener {
            //完成
            countDownTimer?.cancel()
            guardEndTime= 0
            refreshGuardState()
            iv_finish.visibility=View.GONE
            EventBus.getDefault().post(RefreshGuardState())
        }
    }

    private fun refreshGuardState() {
        if (System.currentTimeMillis() < guardEndTime) {
            //守护开启中
            countDownTimer = object : CountDownTimer(guardEndTime - System.currentTimeMillis(), 1000) {
                override fun onTick(p0: Long) {
                    tv_time.text = DateUtils.longTimeToHMS(p0)
                }

                override fun onFinish() {
                    tv_time.visibility= View.INVISIBLE
                    tv_state.text = "泰安全守护完成"
                    iv_finish.visibility=View.GONE
                }

            }
            countDownTimer?.start()
        } else {
            tv_time.visibility= View.INVISIBLE
            tv_state.text = "泰安全守护完成"
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}