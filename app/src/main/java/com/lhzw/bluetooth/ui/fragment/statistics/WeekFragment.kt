package com.lhzw.bluetooth.ui.fragment.statistics

import android.view.View
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseFragment
import com.lhzw.bluetooth.bean.CurrentDataBean
import com.lhzw.bluetooth.bean.DailyInfoDataBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.XAxisValueFormatter
import com.lhzw.bluetooth.widget.XYMarkerView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_daily_statistics.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_week.*
import kotlinx.android.synthetic.main.fragment_week.tv_cal_num
import kotlinx.android.synthetic.main.fragment_week.tv_step_num
import org.joda.time.LocalDate
import org.litepal.LitePal
import org.litepal.extension.findAll

/**
 * Created by heCunCun on 2020/6/23
 */
class WeekFragment : BaseFragment() {
    override fun attachLayoutRes(): Int = R.layout.fragment_week

    override fun initView(view: View) {
        tv_step_num.typeface = Constants.font_futurn_num
        tv_cal_num.typeface = Constants.font_futurn_num
        initBarChart(bar_step, resources.getColor(R.color.color_gray_757575))
        initBarChart(bar_cal, resources.getColor(R.color.color_gray_757575))
        getOneWeekData()

    }
    private fun getOneWeekData() {
        Thread(Runnable {
            val todayDataAndWeekdayBefore = DateUtils.getTodayDataAndWeekdayBefore()//2020-7-7,1
            val split = todayDataAndWeekdayBefore.split(",")
            val dataString =split[0]//20-1-18
            val days=split[1].toInt()//前面第几天是周一
            val stepValues = ArrayList<BarEntry>()//7天步数集合
            val calValues = ArrayList<BarEntry>()//7天cal集合
            Logger.e("当前日期==$dataString")
            val monday = DateUtils.getDateMinusStr(dataString, days)
            Logger.e("当前周一的日期==$monday")
            var weekStepTotal = 0f//周步数
            var weekCalTotal = 0f//周cal
            for(i in 0..6){
                //Logger.e("本周日期==${DateUtils.getDatePlusStr(monday,i)}")
                //当前天的数据从动态数据拿
                if (i==days){//当前日期
                    //查询当前步数,cal
                    var currentStepNum = 0f
                    var currentCalNum = 0f
                    val currentList = LitePal.findAll<CurrentDataBean>()
                    if (currentList.isNotEmpty()) {
                        currentStepNum = (currentList[0].dailyStepNumTotal + currentList[0].sportStepNumTotal).toFloat()
                        currentCalNum = (currentList[0].dailyCalTotal + currentList[0].sportCalTotal).toFloat()
                    }
                    weekStepTotal+=currentStepNum
                    weekCalTotal+=currentCalNum
                    stepValues.add(BarEntry(i.toFloat(),currentStepNum))
                    calValues.add(BarEntry(i.toFloat(), currentCalNum))
                }else{
                    weekStepTotal+=getOnDayStepData(DateUtils.getDatePlusStr(monday,i))
                    weekCalTotal+= getOnDayCalData(DateUtils.getDatePlusStr(monday,i))
                    stepValues.add(BarEntry(i.toFloat(), getOnDayStepData(DateUtils.getDatePlusStr(monday,i))))
                    calValues.add(BarEntry(i.toFloat(), getOnDayCalData(DateUtils.getDatePlusStr(monday,i))))
                }

            }
            activity?.runOnUiThread {
                tv_step_num?.text=weekStepTotal.toInt().toString()
                tv_cal_num?.text=weekCalTotal.toInt().toString()
                initBarData(bar_step, stepValues, resources.getColor(R.color.green_path), resources.getColor(R.color.green_33CC99))
                initBarData(bar_cal, calValues, resources.getColor(R.color.color_pink_FF00FF), resources.getColor(R.color.color_pink_FF0099))
            }

        }).start()


    }
//获取1天总步数
    private fun getOnDayStepData(dataString:String):Float {
        var sumStep=0f
        val daily = dataString.substring(2, dataString.length)
        val dailyInfoList = LitePal.where("daily_date = ?", daily).find(DailyInfoDataBean::class.java)
        if(dailyInfoList.isNotEmpty()){
            for (i in 0..23) {
                sumStep+= (dailyInfoList[i].daily_steps+dailyInfoList[i].sport_steps)
            }
        }
    //Logger.e("$daily 的步数==$sumStep")
        return sumStep
    }
//获取1天总cal
    private fun getOnDayCalData(dataString:String):Float {
        var sumCal=0f
        val daily = dataString.substring(2, dataString.length)
        val dailyInfoList = LitePal.where("daily_date = ?", daily).find(DailyInfoDataBean::class.java)
        if(dailyInfoList.isNotEmpty()){
            for (i in 0..23) {
                sumCal+= (dailyInfoList[i].daily_calorie+dailyInfoList[i].sport_calorie)
            }
        }
        return sumCal
    }


    override fun lazyLoad() {

    }

    private fun initBarChart(barChat: BarChart, mainColor: Int) {
        val times = arrayOf("周一", "·", "·", "·", "·", "·", "周日")

        barChat.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                //选中值
                //Logger.e("value=${e.toString()}")
            }
        })

        barChat.apply {
            description.isEnabled = false
            setMaxVisibleValueCount(7)//最大显示24个值
            setPinchZoom(false)//手指缩放
            setDrawBarShadow(true)//画条形图背景

            setDrawGridBackground(false)
            setDrawValueAboveBar(false)
            setScaleEnabled(false)
            extraBottomOffset = 0f
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                gridColor = resources.getColor(R.color.white)
                textColor = mainColor
                valueFormatter = XAxisValueFormatter(times)
                axisLineColor = mainColor
                setLabelCount(7, false)//设置标签个数,true为精确
            }

            axisLeft.apply {
                isEnabled = true
                setDrawLabels(false)//标签
                setDrawAxisLine(false)//轴线
                setDrawGridLines(false)//网格线
                gridColor = resources.getColor(R.color.gray)
                axisMinimum = 0f
                //  enableGridDashedLine(20f, 5f, 0f)
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
            val mv = XYMarkerView(activity, XAxisValueFormatter(times))
            mv.chartView = barChat
            marker = mv
        }

    }

    private fun initBarData(barChat: BarChart, values: ArrayList<BarEntry>, startColor: Int, endColor: Int) {
        val set1: BarDataSet?
        if (barChat.data != null && barChat.data.dataSetCount > 0) {
            set1 = barChat.data.getDataSetByIndex(0) as BarDataSet
            set1.values = values
            set1.notifyDataSetChanged()
            barChat.data.notifyDataChanged()
            barChat.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "Data Set")
            // * 可变参数展开操作符
            // set1.setColors(*ColorTemplate.VORDIPLOM_COLORS)
            set1.setGradientColor(startColor, endColor)
            if (barChat == bar_step) {
                set1.barShadowColor = resources.getColor(R.color.color_green_0C1A00)
            } else {
                set1.barShadowColor = resources.getColor(R.color.color_red_180008)
            }

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)
            val data = BarData(dataSets)
            data.barWidth = 0.6f
            barChat.data = data
            barChat.setFitBars(true) // 在bar开头结尾两边添加一般bar宽的留白
        }
        barChat.invalidate()
    }

}