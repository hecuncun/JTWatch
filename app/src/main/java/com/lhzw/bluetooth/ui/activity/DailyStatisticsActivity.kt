package com.lhzw.bluetooth.ui.activity

import android.view.View
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.lhzw.bluetooth.bean.DailyDataBean
import com.lhzw.bluetooth.bean.DailyInfoDataBean
import com.lhzw.bluetooth.uitls.DateUtils
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_daily_statistics.*
import kotlinx.android.synthetic.main.toolbar.*
import org.joda.time.LocalDate
import org.litepal.LitePal
import org.litepal.extension.findAll

/**
 * Created by heCunCun on 2019/11/18
 */
class DailyStatisticsActivity : BaseActivity() {
    private var today = LocalDate.now().toString()//2019-11-20
    override fun attachLayoutRes(): Int = R.layout.activity_daily_statistics

    override fun initData() {



    }

    override fun initView() {
        im_back.visibility = View.VISIBLE
        toolbar_title.text = "日常统计"
        im_back.setOnClickListener {
            finish()
        }
        getTotalData(today)
        //先查有几天数据
        val list = LitePal.findAll<DailyDataBean>()
        if(list.isNotEmpty()){
            monthCalendar.setDateInterval(DateUtils.getDateMinusStr(today,list.size), today)//设置日期可选范围
        }

    }

    private fun getTotalData(dataString:String) {
        //获取当天24小时的信息list
        val dateNow =dataString//选中的日期
        Logger.e("选中$dateNow")
        val list = dateNow.split("-")
        val sb = StringBuilder()
        val year = list[0].substring(2, 4).toInt().toString()
        val month = list[1].toInt().toString()
        val day = list[2].toInt().toString()
        val daily_date = sb.append(year).append("-").append(month).append("-").append(day).trim().toString()//20-1-18
        //24小时数据
        val dailyInfoList = LitePal.where("daily_date = ?", daily_date).find(DailyInfoDataBean::class.java)
        //初始化24小时步数图表的值
        var sumStep=0
        var sumCal=0
        if(dailyInfoList.isNotEmpty()){
            for (i in 0..23) {
                sumStep+= (dailyInfoList[i].daily_steps+dailyInfoList[i].sport_steps)
            }
            //初始化24小时cal表的值

            for (i in 0..23) {
                sumCal+= (dailyInfoList[i].daily_calorie+dailyInfoList[i].sport_calorie)
            }
        }
        tv_step_num.text=sumStep.toString()
        tv_cal_num.text=sumCal.toString()
    }

    override fun initListener() {
        monthCalendar.setOnCalendarChangedListener { _, _, _, localDate ->
            tv_select_time.text = "$localDate"
            getTotalData(localDate.toString())
        }
    }
}