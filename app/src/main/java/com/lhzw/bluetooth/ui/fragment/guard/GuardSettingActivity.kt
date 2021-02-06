package com.lhzw.bluetooth.ui.fragment.guard

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.view.MotionEvent
import android.view.View
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.RefreshGuardState
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.uitls.Preference
import kotlinx.android.synthetic.main.activity_guard_running_map.*
import kotlinx.android.synthetic.main.activity_guard_setting.*
import kotlinx.android.synthetic.main.activity_guard_setting.iv_back
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Created by heCunCun on 2021/2/6
 */
class GuardSettingActivity:BaseActivity() {
    private var guardTime = 60//分钟
    private var guardEndTime: Long by Preference(Constants.GUARD_END_TIME, 0)//守护结束时间
    override fun attachLayoutRes(): Int= R.layout.activity_guard_setting

    override fun initData() {
    }

    override fun initView() {
    }

    private var scheduledExecutor:ScheduledExecutorService?=null
    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when ( msg.what) {
                R.id.iv_reduce -> {
                    guardTime--
                    if (guardTime<1){
                        guardTime=1
                        showToast("已到达最少守护时长")
                    }
                    tv_total_time.text = guardTime.toString()
                }//减小操作
                R.id.iv_add -> {
                    guardTime++
                    if (guardTime>1440){
                        guardTime=1440
                        showToast("已到达最大守护时长")
                    }
                    tv_total_time.text = guardTime.toString()
                } //增大操作
            }
        }
    }
    private fun stopAddOrSubtract() {
        if (scheduledExecutor != null) {
            scheduledExecutor!!.shutdownNow();
            scheduledExecutor = null
        }
    }

    private fun updateAddOrSubtract(id: Int) {
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
        scheduledExecutor!!.scheduleWithFixedDelay(Runnable {
            val msg = Message()
            msg.what = id
            handler.sendMessage(msg)
        }, 0, 100, TimeUnit.MILLISECONDS)
    }

    override fun initListener() {
        iv_reduce.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    updateAddOrSubtract(v.id)//手指按下时触发不停的发送消息
                } else if (event.action == MotionEvent.ACTION_UP) {
                    stopAddOrSubtract() //手指抬起时停止发送
                }
                return true
            }

        })

        iv_add.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    updateAddOrSubtract(v.id)//手指按下时触发不停的发送消息
                } else if (event.action == MotionEvent.ACTION_UP) {
                    stopAddOrSubtract() //手指抬起时停止发送
                }
                return true
            }

        })

        iv_start.setOnClickListener {
            //开始倒计时
            finish()
            //设置记录开始守护的时间
            guardEndTime = System.currentTimeMillis()+  guardTime*60*1000
            EventBus.getDefault().post(RefreshGuardState())

        }
        iv_back.setOnClickListener {
            finish()
        }
    }
}