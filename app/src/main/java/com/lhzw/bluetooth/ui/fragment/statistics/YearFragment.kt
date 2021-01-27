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
import kotlinx.android.synthetic.main.fragment_year.*
import kotlinx.android.synthetic.main.fragment_year.bar_cal
import kotlinx.android.synthetic.main.fragment_year.bar_step
import kotlinx.android.synthetic.main.fragment_year.tv_cal_num
import kotlinx.android.synthetic.main.fragment_year.tv_step_num
import org.litepal.LitePal
import org.litepal.extension.findAll

/**
 * Created by heCunCun on 2020/6/23
 */
class YearFragment:BaseFragment() {
    override fun attachLayoutRes(): Int= R.layout.fragment_year

    override fun initView(view: View) {
        tv_step_num.typeface= Constants.font_futurn_num
        tv_cal_num.typeface= Constants.font_futurn_num
        initBarChart(bar_step, resources.getColor(R.color.color_gray_757575))
        initBarChart(bar_cal, resources.getColor(R.color.color_gray_757575))
        getOneYearData()
//        val stepValues = ArrayList<BarEntry>()
//        for (i in 0..11) {
//            val num = (Math.random() * 1000).toFloat()
//            //   val num = dailyInfoList[i].daily_steps.toFloat()+dailyInfoList[i].sport_steps.toFloat()
//            stepValues.add(BarEntry(i.toFloat(), num))
//        }
//        Logger.e("加载month数据")
//        initBarData(bar_step, stepValues, resources.getColor(R.color.green_path), resources.getColor(R.color.green_33CC99))
//        initBarData(bar_cal, stepValues, resources.getColor(R.color.color_pink_FF00FF), resources.getColor(R.color.color_pink_FF0099))
    }

    private fun getOneYearData() {
        Thread(Runnable {
            //先算当前的月步数和cal
            val todayDataAndWeekdayBefore = DateUtils.getTodayDataAndWeekdayBefore()
            val split = todayDataAndWeekdayBefore.split(",")
            val dataString = split[0]//2020-1-18
            val year = dataString.split("-")[0].toInt()
            val month = dataString.split("-")[1]
            // Logger.e("当前日期==$dataString")

            var yearStepTotal = 0f//年步数
            var yearCalTotal = 0f//年cal

            val stepValues = ArrayList<BarEntry>()//12个月步数集合
            val calValues = ArrayList<BarEntry>()//12个月cal集合


            for (i in 0..11) {
                //当前天的数据从动态数据拿
                if (i == (month.toInt()-1)) {//当前月
                    //查询当前天步数,cal
                    var currentStepNum = 0f
                    var currentCalNum = 0f
                    val currentList = LitePal.findAll<CurrentDataBean>()
                    if (currentList.isNotEmpty()) {
                        currentStepNum = (currentList[0].dailyStepNumTotal + currentList[0].sportStepNumTotal).toFloat()
                        currentCalNum = (currentList[0].dailyCalTotal + currentList[0].sportCalTotal).toFloat()
                    }
                    val currentMonthStepData = getOneMonthStepData((i + 1).toString(), year)+currentStepNum
                    val currentMonthCalData = getOneMonthCalData((i + 1).toString(), year)+currentCalNum
                    yearStepTotal += currentMonthStepData
                    yearCalTotal += currentMonthCalData
                    stepValues.add(BarEntry(i.toFloat(), currentMonthStepData))
                    calValues.add(BarEntry(i.toFloat(), currentMonthCalData))
                } else {
                    yearStepTotal += getOneMonthStepData((i+1).toString(),year)
                    yearCalTotal +=  getOneMonthCalData((i+1).toString(),year)
                    stepValues.add(BarEntry(i.toFloat(), getOneMonthStepData((i+1).toString(),year)))
                    calValues.add(BarEntry(i.toFloat(),  getOneMonthCalData((i+1).toString(),year)))
                }

            }
           activity?.runOnUiThread {
                tv_step_num?.text = yearStepTotal.toInt().toString()
                tv_cal_num?.text = yearCalTotal.toInt().toString()
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

    /**
     * 获取某个月的步数总和
     */
    private fun getOneMonthStepData(month:String,year:Int):Float{
        //year  2020   month  7
        var sumStep = 0f
        val monthSize = getMonthSize(month, year)
        val firstDay ="$year-$month-1"

        for (i in 0 until monthSize) {//2020-7-1
            sumStep+= getOnDayStepData(DateUtils.getDatePlusStr(firstDay, i))
        }
       // Logger.e("年统计,本月1号日期==$year-$month-1,步数==$sumStep")
        return sumStep
    }
    /**
     * 获取某个月的步数总和
     */
    private fun getOneMonthCalData(month:String,year:Int):Float{
        //year  2020   month  7
        var sumCal = 0f
        val monthSize = getMonthSize(month, year)
        val firstDay ="$year-$month-1"
       // Logger.e("年统计,本月1号日期==$year-$month-1")
        for (i in 0 until monthSize) {//2020-7-1
            sumCal+= getOnDayCalData(DateUtils.getDatePlusStr(firstDay, i))
        }
        return sumCal
    }



    //获取1天总步数
    private fun getOnDayStepData(dataString: String): Float {
        var sumStep = 0f
        val daily = dataString.substring(2, dataString.length)
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
        val times = arrayOf("一月", "·", "·", "·", "·","六月" ,"·","·","·","·","·", "十二月")

        barChat.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                //选中值
                Logger.e("value=${e.toString()}")
            }
        })

        barChat.apply {
            description.isEnabled = false
            setMaxVisibleValueCount(12)//最大显示24个值
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
                setLabelCount(12, false)//设置标签个数,true为精确
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

    private fun initBarData(barChat: BarChart?, values: ArrayList<BarEntry>, startColor: Int, endColor: Int) {
        val set1: BarDataSet?
        if (barChat?.data != null && barChat.data!!.dataSetCount > 0) {
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
            data.barWidth = 0.5f
            barChat?.data = data
            barChat?.setFitBars(true) // 在bar开头结尾两边添加一般bar宽的留白
        }
        barChat?.invalidate()
    }
}