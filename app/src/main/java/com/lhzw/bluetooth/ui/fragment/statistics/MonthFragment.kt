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
import kotlinx.android.synthetic.main.fragment_month.*
import org.litepal.LitePal
import org.litepal.extension.findAll

/**
 * Created by heCunCun on 2020/6/23
 */
class MonthFragment : BaseFragment() {
    override fun attachLayoutRes(): Int = R.layout.fragment_month

    override fun initView(view: View) {
        tv_step_num.typeface = Constants.font_futurn_num
        tv_cal_num.typeface = Constants.font_futurn_num
        initBarChart(bar_step, resources.getColor(R.color.color_gray_757575))
        initBarChart(bar_cal, resources.getColor(R.color.color_gray_757575))
        getOneMonthData()
    }

    private fun getOneMonthData() {
        Thread(Runnable {
            val todayDataAndWeekdayBefore = DateUtils.getTodayDataAndWeekdayBefore()
            val split = todayDataAndWeekdayBefore.split(",")
            val dataString = split[0]//2020-1-18
            val year = dataString.split("-")[0].toInt()
            val month = dataString.split("-")[1]
            val days = dataString.split("-")[2].toInt() - 1//前面第几天是1号
            val monthSize = getMonthSize(month, year)
            Logger.e("当前日期==$dataString")
            Logger.e("本月==$month,本日=$days")
            // val days=split[1].toInt()//前面第几天是周一
            val stepValues = ArrayList<BarEntry>()//月步数集合
            val calValues = ArrayList<BarEntry>()//月cal集合
            val monthOne = DateUtils.getDateMinusStr(dataString, days)
            Logger.e("当前月一号的日期==$monthOne")
            var monthStepTotal = 0f//月步数
            var monthCalTotal = 0f//月cal

            for (i in 0 until monthSize) {
                //  Logger.e("本月日期==${DateUtils.getDatePlusStr(monthOne, i)}")
                //当前天的数据从动态数据拿
                if (i == days) {//当前日期
                    //查询当前步数,cal
                    var currentStepNum = 0f
                    var currentCalNum = 0f
                    val currentList = LitePal.findAll<CurrentDataBean>()
                    if (currentList.isNotEmpty()) {
                        currentStepNum = (currentList[0].dailyStepNumTotal + currentList[0].sportStepNumTotal).toFloat()
                        currentCalNum = (currentList[0].dailyCalTotal + currentList[0].sportCalTotal).toFloat()
                    }
                    monthStepTotal += currentStepNum
                    monthCalTotal += currentCalNum
                    stepValues.add(BarEntry(i.toFloat(), currentStepNum))
                    calValues.add(BarEntry(i.toFloat(), currentCalNum))
                } else {
                    monthStepTotal += getOnDayStepData(DateUtils.getDatePlusStr(monthOne, i))
                    monthCalTotal += getOnDayCalData(DateUtils.getDatePlusStr(monthOne, i))
                    stepValues.add(BarEntry(i.toFloat(), getOnDayStepData(DateUtils.getDatePlusStr(monthOne, i))))
                    calValues.add(BarEntry(i.toFloat(), getOnDayCalData(DateUtils.getDatePlusStr(monthOne, i))))
                }

            }
            activity?.runOnUiThread {
                tv_step_num?.text = monthStepTotal.toInt().toString()
                tv_cal_num?.text = monthCalTotal.toInt().toString()
                initBarData(bar_step, stepValues, resources.getColor(R.color.green_path), resources.getColor(R.color.green_33CC99))
                initBarData(bar_cal, calValues, resources.getColor(R.color.color_pink_FF00FF), resources.getColor(R.color.color_pink_FF0099))
            }

        }).start()


    }

    /**
     * 获取一个月的天数
     */
    private fun getMonthSize(month: String, year: Int): Int {
        val months = mutableListOf("1", "3", "5", "7", "8", "10", "12")
        var monthSize = 30
        if (months.contains(month)) {
            monthSize = 31
        }
        if (month == "2") {
            if (year % 4 == 0) {
                monthSize = 29
            } else {
                monthSize = 28
            }
        }
        return monthSize
    }

    //获取1天总步数
    private fun getOnDayStepData(dataString: String): Float {//2020-7-6
        var sumStep = 0f
        val daily = dataString.substring(2, dataString.length)//20-7-6
        val dailyInfoList = LitePal.where("daily_date = ?", daily).find(DailyInfoDataBean::class.java)
        if (dailyInfoList.isNotEmpty()) {
            for (i in 0..23) {
                sumStep += (dailyInfoList[i].daily_steps + dailyInfoList[i].sport_steps)
            }
        }
        return sumStep
    }

    //获取1天总cal
    private fun getOnDayCalData(dataString: String): Float {
        var sumCal = 0f
        val daily = dataString.substring(2, dataString.length)
        val dailyInfoList = LitePal.where("daily_date = ?", daily).find(DailyInfoDataBean::class.java)
        if (dailyInfoList.isNotEmpty()) {
            for (i in 0..23) {
                sumCal += (dailyInfoList[i].daily_calorie + dailyInfoList[i].sport_calorie)
            }
        }
        return sumCal
    }

    override fun lazyLoad() {

    }

    private fun initBarChart(barChat: BarChart, mainColor: Int) {
        val todayDataAndWeekdayBefore = DateUtils.getTodayDataAndWeekdayBefore()
        val split = todayDataAndWeekdayBefore.split(",")
        val dataString = split[0]//20-1-18
        val year = dataString.split("-")[0].toInt()
        val month = dataString.split("-")[1]
        var monthSize = getMonthSize(month, year)
        val times = Array(monthSize){""}
        for (i in 1..monthSize) {
            if (i == 1 || i == times.size || i == 15) {
                times[i - 1] = i.toString()
            } else {
                times[i - 1] = "·"
            }

        }

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
            setMaxVisibleValueCount(monthSize)//最大显示24个值
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
                setLabelCount(monthSize, false)//设置标签个数,true为精确
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
            data.barWidth = 0.4f
            barChat.data = data
            barChat.setFitBars(true) // 在bar开头结尾两边添加一般bar宽的留白
        }
        barChat.invalidate()
    }
}