package com.lhzw.bluetooth.ui.fragment

import android.bluetooth.BluetoothManager
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseFragment
import com.lhzw.bluetooth.bean.CurrentDataBean
import com.lhzw.bluetooth.bean.DailyInfoDataBean
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.ConnectEvent
import com.lhzw.bluetooth.event.HideDialogEvent
import com.lhzw.bluetooth.event.RefreshTargetStepsEvent
import com.lhzw.bluetooth.event.SyncDataEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.service.BleConnectService
import com.lhzw.bluetooth.ui.activity.DailyStatisticsActivity
import com.lhzw.bluetooth.ui.activity.StatisticsActivity
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.uitls.XAxisValueFormatter
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fragment_home.*
import me.jessyan.autosize.internal.CancelAdapt
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.extension.findAll
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by hecuncun on 2019/11/13
 */
class HomeFragment : BaseFragment(),CancelAdapt{
    private var bleManager: BluetoothManager? = null
    private var state = false
    private var syncTime: String by Preference(Constants.SYNC_TIME, "")//最近同步时间
    override fun useEventBus() = true

    companion object {
        fun getInstance(): HomeFragment = HomeFragment()
    }

    override fun attachLayoutRes(): Int = R.layout.fragment_home

    override fun initView(view: View) {
        initLineChar(step_line_chart)
        initLineChar(cal_line_chart)
        tv_current_step_chart.typeface=Constants.font_futurn_num
        tv_current_cal_chart.typeface=Constants.font_futurn_num
        tv_step_chart.typeface=Constants.font_futurn_num
        tv_cal_chart.typeface=Constants.font_futurn_num
        ll_progress_container.setOnClickListener {
            //跳转统计页
            startActivity(Intent(activity,StatisticsActivity::class.java))
        }


    }

    //初始化图表
    private fun initLineChar(lineChart: LineChart) {
        val times = arrayOf("1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00",
                "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "24:00")

        lineChart.apply {
            fitScreen()
            setTouchEnabled(true)//触摸事件
            setDrawGridBackground(false)//网格线
            setNoDataText("暂无数据")
            isDragEnabled = true//可拖拽
            isScaleXEnabled = false//X缩放
            isScaleYEnabled = false//Y缩放
            setPinchZoom(false)//手指缩放
            //配置X轴
            xAxis.apply {
                setLabelCount(6, false)
                granularity=1f
                enableGridDashedLine(10f, 10f, 0f)//垂直虚线
                // xAxis.enableAxisLineDashedLine(10f, 10f, 0f)  X轴线
                setDrawAxisLine(false)//是否画X轴线
                textColor=resources.getColor(R.color.gray)
                position = XAxis.XAxisPosition.BOTTOM//X轴位置
                valueFormatter = XAxisValueFormatter(times)
            }
            //隐藏
            description.isEnabled = false//右上角描述
            legend.isEnabled = false//隐藏左下角label
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            //valuesToHighlight()
        }

        lineChart.setOnChartValueSelectedListener(object :OnChartValueSelectedListener{
            override fun onNothingSelected() {
            }

            override fun onValueSelected(e: Entry, h: Highlight?) {
                Logger.e("${e.toString()}")
                var position =e.x.toInt()
                if (lineChart == step_line_chart ){
                        tv_step_chart.text=e.y.toInt().toString()
                    //自定义方法设置圆点显示
                        LineChartRenderer.setCirclePoints(position)
                }else{
                    tv_cal_chart.text=e.y.toInt().toString()
                    //自定义方法设置圆点显示
                    LineChartRenderer.setCirclePoints(position)
                }
                lineChart.invalidate()
            }

        })

    }

//    private fun initBarChart(barChat: BarChart, mainColor: Int) {
//        val times = arrayOf("1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00",
//                "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "24:00")
//
//        barChat.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//            override fun onNothingSelected() {
//            }
//
//            override fun onValueSelected(e: Entry?, h: Highlight?) {
//                //选中值
//                Logger.e("value=${e.toString()}")
//            }
//        })
//
//        barChat.apply {
//            description.isEnabled = false
//            setMaxVisibleValueCount(24)//最大显示24个值
//            setPinchZoom(false)
//            setDrawBarShadow(false)
//            setDrawGridBackground(false)
//            setDrawValueAboveBar(false)
//            setScaleEnabled(false)
//            extraBottomOffset = 0f
//            xAxis.apply {
//                position = XAxis.XAxisPosition.BOTTOM
//                setDrawGridLines(false)
//                gridColor = resources.getColor(R.color.white)
//                textColor = mainColor
//                valueFormatter = XAxisValueFormatter(times)
//                axisLineColor = resources.getColor(R.color.colorPrimary)
//                setLabelCount(6, true)//设置标签个数,true为精确
//            }
//
//            axisLeft.apply {
//                isEnabled = true
//                setDrawLabels(false)//标签
//                setDrawAxisLine(false)//轴线
//                setDrawGridLines(true)//网格线
//                gridColor = resources.getColor(R.color.gray)
//                axisMinimum = 0f
//                enableGridDashedLine(20f, 5f, 0f)
//            }
//            axisRight.isEnabled = false
//            legend.isEnabled = false
//            val mv = XYMarkerView(activity, XAxisValueFormatter(times))
//            mv.chartView = barChat
//            marker = mv
//        }
//
//    }


    private fun jumpToDailyStatisticsActivity() {
        val intent = Intent(activity, DailyStatisticsActivity::class.java)
        startActivity(intent)
    }

    override fun lazyLoad() {
        if (connectState) {
            //处于连接状态,就显示数据
            setConnectedState()
            //初始化手表数据
            initWatchData()
        } else {
            //显示空白页
            setDisConnectState()

        }

    }

//    private fun initBarData(barChat: BarChart, values: ArrayList<BarEntry>, startColor: Int, endColor: Int) {
//        val set1: BarDataSet?
//        if (barChat.data != null && barChat.data.dataSetCount > 0) {
//            set1 = barChat.data.getDataSetByIndex(0) as BarDataSet
//            set1.values = values
//            barChat.notifyDataSetChanged()
//        } else {
//            set1 = BarDataSet(values, "Data Set")
//            // * 可变参数展开操作符
//            // set1.setColors(*ColorTemplate.VORDIPLOM_COLORS)
//            set1.setGradientColor(startColor, endColor)
//            val dataSets = ArrayList<IBarDataSet>()
//            dataSets.add(set1)
//            val data = BarData(dataSets)
//            data.barWidth = 0.3f
//            barChat.data = data
//            barChat.setFitBars(true) // 在bar开头结尾两边添加一般bar宽的留白
//        }
//        barChat.invalidate()
//    }

    private fun initLineData(chart: LineChart, values: ArrayList<Entry>, startColor: Int, endColor: Int) {
        val set1: LineDataSet?
        if (chart.data != null && chart.data.dataSetCount > 0) {
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = values
            set1.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else { // create a dataset and give it a type
            set1 = LineDataSet(values, "DataSet 1")
            //set1.mode = LineDataSet.Mode.CUBIC_BEZIER// 设置线条的模式
             set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER// 设置线条的模式
           // set1.mode = LineDataSet.Mode.CUBIC_BEZIER// 设置线条的模式
            set1.setDrawIcons(false)
            set1.setDrawValues(false)
            set1.setDrawHighlightIndicators(false)//设置高亮辅助线
            // draw dashed line
            // set1.enableDashedLine(10f, 5f, 0f)
            // black lines and points
           // set1.setColor(Color.RED)
           set1.setGradientColor(resources.getColor(startColor), resources.getColor(endColor))//设置曲线的颜色
            set1.circleHoleColor = resources.getColor(R.color.gray_bottom)  //圆孔颜色
            // line thickness and point size
            set1.lineWidth = 2f//线宽
            set1.circleRadius = 5f//圆半径
            set1.circleHoleRadius=3f
            // draw points as solid circles
            set1.setDrawCircleHole(true)//圆孔
            set1.setDrawCircles(true)//圆
            // customize legend entry
           // set1.setFormLineWidth(1f)
           // set1.setFormLineDashEffect(DashPathEffect(floatArrayOf(10f, 5f), 0f))
          //  set1.setFormSize(15f)
            // text size of values
           // set1.setValueTextSize(9f)
            // draw selection line as dashed
            //set1.enableDashedHighlightLine(10f, 5f, 0f)
            // set the filled area
//            set1.setDrawFilled(false)
//            set1.setFillFormatter(IFillFormatter { dataSet, dataProvider -> chart.getAxisLeft().getAxisMinimum() })
            // set color of filled area
//            if (Utils.getSDKInt() >= 18) { // drawables only supported on api level 18 and above
//                val drawable: Drawable? = ContextCompat.getDrawable(activity, R.drawable.fade_red)
//                set1.setFillDrawable(drawable)
//            } else {
//                set1.setFillColor(Color.BLACK)
//            }
            val dataSets = java.util.ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets
            // create a data object with the data sets
            val data = LineData(dataSets)
            // set data
            chart.data = data
        }

        chart.invalidate()
    }


    // 连接成功后初始化手表数据
    private fun initWatchData() {
        //刷新目标步数/cal和当前占比
        refreshTargetSteps(RefreshTargetStepsEvent())

        //获取当天24小时的信息list
        val dateNow = DateUtils.longToString(System.currentTimeMillis(), "yyyy-MM-dd")
        val list = dateNow.split("-")
        val sb = StringBuilder()
        val year = list[0].substring(2, 4).toInt().toString()
        val month = list[1].toInt().toString()
        val day = list[2].toInt().toString()
        val daily_date = sb.append(year).append("-").append(month).append("-").append(day).trim().toString()//20-1-18
        //24小时数据
        val dailyInfoList = LitePal.where("daily_date = ?", daily_date).find(DailyInfoDataBean::class.java)
        //初始化24步数数据
        if (dailyInfoList.isNotEmpty()) {
            val stepValues = ArrayList<Entry>()
            for (i in 0..23) {
                //val num = (Math.random() * 1000).toFloat()
                val num = dailyInfoList[i].daily_steps.toFloat() + dailyInfoList[i].sport_steps.toFloat()
                stepValues.add(Entry(i.toFloat(), num))
                //Logger.e("$i 小时,步数==$num")
            }

            initLineData(step_line_chart, stepValues, R.color.green_path, R.color.blue_path)
            //初始化24小时cal表的值
            val calValues = ArrayList<Entry>()
            for (i in 0..23) {
               // val num = (Math.random() * 1000).toFloat()
                val num = dailyInfoList[i].daily_calorie.toFloat() + dailyInfoList[i].sport_calorie.toFloat()
                calValues.add(Entry(i.toFloat(), num))
            }
            initLineData(cal_line_chart, calValues, R.color.color_pink_FF00FF, R.color.color_red_FF0000)

                //初始化24小时步数图表的值
//        if (dailyInfoList.isNotEmpty()){
//            val stepValues = ArrayList<BarEntry>()
//            for (i in 0..23) {
//                //val num = (Math.random() * 1000).toFloat()
//                val num = dailyInfoList[i].daily_steps.toFloat()+dailyInfoList[i].sport_steps.toFloat()
//                stepValues.add(BarEntry(i.toFloat(), num))
//            }

//            }
                //initBarData(bar_chart_step, stepValues, resources.getColor(R.color.blue_light), resources.getColor(R.color.green_circle))
                //initBarData(bar_chart_cal, calValues, resources.getColor(R.color.orange_light), resources.getColor(R.color.orange))
        }
    }

    //    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun refresh(event: RefreshEvent) {
//        when (event.type) {
//            Constants.TYPE_CURRENT_DATA -> {
//                val list = LitePal.findAll(CurrentDataBean::class.java)
//                if (list.isNotEmpty()) {
//                    //当前步数
//                    //Logger.e("当前步数更新==${list[0].dailyStepNumTotal.toString()}")
//                    tv_step_num.text = (list[0].dailyStepNumTotal+ list[0].sportStepNumTotal).toString()
//                    //当前卡路里
//                    tv_cal_num.text =(list[0].dailyCalTotal+list[0].sportCalTotal).toString()
//                }
//
//            }
//
//
//        }
//    }
//
//
    private var targetStepNum = 0
    private var targetCalNum = 0
    private var currentStepNum = 0
    private var currentCalNum = 0

    //刷新当前步数,cal百分比
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshTargetSteps(event: RefreshTargetStepsEvent) {
        //查询当前步数,cal
        val currentList = LitePal.findAll<CurrentDataBean>()
        if (currentList.isNotEmpty()) {
            tv_step_num.text = (currentList[0].dailyStepNumTotal + currentList[0].sportStepNumTotal).toString()//进度条步数
            tv_current_step_chart.text=(currentList[0].dailyStepNumTotal + currentList[0].sportStepNumTotal).toString()//折线图步数
            tv_cal_num.text = (currentList[0].dailyCalTotal + currentList[0].sportCalTotal).toString()//进度条步数
            tv_current_cal_chart.text= (currentList[0].dailyCalTotal + currentList[0].sportCalTotal).toString()//折线图步数
            currentStepNum = currentList[0].dailyStepNumTotal + currentList[0].sportStepNumTotal
            currentCalNum = currentList[0].dailyCalTotal + currentList[0].sportCalTotal
        }
        //查询目标步数,cal
        val list = LitePal.findAll<PersonalInfoBean>()
        if (list.isNotEmpty()) {
            targetStepNum = list[0].des_steps
            targetCalNum = list[0].des_calorie
        }
        if (targetStepNum > 0) {
            task_step.setProgress(currentStepNum * 100 / targetStepNum)
            tv_step_percent.text = "${currentStepNum * 100 / targetStepNum}%"
            tv_step_percent.typeface = Constants.font_futurn_num
        }
        if (targetCalNum > 0) {
            task_cal.setProgress(currentCalNum * 100 / (targetCalNum * 1000))
            tv_cal_percent.text = "${currentCalNum * 100 / (targetCalNum * 1000)}%"
            tv_cal_percent.typeface = Constants.font_futurn_num
        }

    }

    //
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onBleStateChanged(event: BleStateEvent) {
//        if (event.state) {//打开
//            tv_ble_state_tip.text = "在连接中添加设备"
//        } else {//关闭
//            tv_ble_state_tip.text = "蓝牙关闭"
//        }
//    }
//
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWatchConnectChanged(event: ConnectEvent) {
        if (event.isConnected) {
            setConnectedState()
            //已连接成功后再显示数据
        } else {
            //未连接
            setDisConnectState()
        }
    }

    /**
     * 设置未连接状态
     */
    private fun setDisConnectState() {
        rl_state_disconnect_cal.visibility = View.VISIBLE
        rl_state_disconnect_step.visibility = View.VISIBLE
        rl_state_connect_cal.visibility = View.GONE
        rl_state_connect_step.visibility = View.GONE
        task_step.setProgress(100)
        task_cal.setProgress(100)
        step_line_chart.clear()
        cal_line_chart.clear()
        tv_step_chart.text="--"
        tv_cal_chart.text="--"
        tv_current_step_chart.text="--"
        tv_current_cal_chart.text="--"
    }

    /**
     * 设置为连接状态
     */
    private fun setConnectedState() {
        rl_state_disconnect_cal.visibility = View.GONE
        rl_state_disconnect_step.visibility = View.GONE
        rl_state_connect_cal.visibility = View.VISIBLE
        rl_state_connect_step.visibility = View.VISIBLE

    }

    //
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideDialog(event: HideDialogEvent) {
        if (event.success){
          //  Logger.e("homeFragment 接收到数据同步成功信息")
            initWatchData()
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && connectState && syncTime.isNotEmpty()){
            //最近同步时间不是 同一天  就同步数据

            val currentTime = DateUtils.longToString(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")
            Logger.e("同步检测  $currentTime  --- $syncTime")
            if (syncTime.split(" ")[0] != currentTime.split(" ")[0]){
                //同步数据
                if (BleConnectService.isConnecting) {
                   showToast( "正在进行同步中，请稍后同步")
                } else {
                    BleConnectService.isConnecting = true
                    showToast( "开始同步")
                    RxBus.getInstance().post("sync", SyncDataEvent("sync"))

                }
            }
        }
    }
}
