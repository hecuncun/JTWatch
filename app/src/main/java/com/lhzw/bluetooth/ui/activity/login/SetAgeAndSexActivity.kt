package com.lhzw.bluetooth.ui.activity.login

import android.content.Intent
import com.jzxiang.pickerview.TimePickerDialog
import com.jzxiang.pickerview.data.Type
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.event.CloseEvent
import com.lhzw.bluetooth.uitls.DateUtils
import kotlinx.android.synthetic.main.activity_set_age_and_sex.*
import kotlinx.android.synthetic.main.fragment_mine.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.extension.find
import org.litepal.extension.findAll

/**
 * Created by heCunCun on 2020/8/12
 */
class SetAgeAndSexActivity : BaseActivity() {
    private var personalInfoBean: PersonalInfoBean? = null//个人信息
    override fun attachLayoutRes(): Int = R.layout.activity_set_age_and_sex
    override fun initData() {
        val list = LitePal.findAll<PersonalInfoBean>()
        personalInfoBean = list[0]

    }

    override fun initView() {

    }

    var age = 25
    private var gender = 1
    override fun initListener() {
        iv_man.setOnClickListener {
            if (gender==1){
                return@setOnClickListener
            }else{
                gender=1
                iv_man.background=resources.getDrawable(R.drawable.rg_checked_bg)
                iv_women.background=null
            }
        }

        iv_women.setOnClickListener {
            if (gender==0){
                return@setOnClickListener
            }else{
                gender=0
                iv_women.background=resources.getDrawable(R.drawable.rg_checked_bg)
                iv_man.background=null
            }
        }


        iv_back.setOnClickListener {
            finish()
        }
        btn_next.setOnClickListener {
            personalInfoBean!!.gender=gender
            personalInfoBean!!.age=age
            personalInfoBean!!.save()
            Intent(this, SetWeightActivity::class.java).apply {
                startActivity(this)
            }
        }

        //日期选择
        val dialogTime = TimePickerDialog.Builder()
                .setCallBack { _, millseconds ->
                    val dateString = DateUtils.longToString(millseconds, "yyyy.MM.dd")
                    tv_birthday.text = dateString
                    val birthYear = DateUtils.longToString(millseconds, "yyyy").toInt()
                    val nowYear = DateUtils.longToString(System.currentTimeMillis(), "yyyy").toInt()
                    age = nowYear - birthYear
                }
                .setCancelStringId("取消")
                .setSureStringId("确定")
                .setTitleStringId("生日")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis() - 100L * 365 * 1000 * 60 * 60 * 24L)
                .setMaxMillseconds(System.currentTimeMillis())
                .setCurrentMillseconds(System.currentTimeMillis() - 25L * 365 * 1000 * 60 * 60 * 24L)
                .setThemeColor(resources.getColor(R.color.orange))
                .setType(Type.YEAR_MONTH_DAY)
                .setWheelItemTextNormalColor(resources.getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(resources.getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(12)
                .build()

        tv_birthday.setOnClickListener {
            dialogTime.show(supportFragmentManager, "a")
        }
    }
    override fun useEventBus(): Boolean =true
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun finishEvent(eventBus: CloseEvent){
        finish()
    }
}