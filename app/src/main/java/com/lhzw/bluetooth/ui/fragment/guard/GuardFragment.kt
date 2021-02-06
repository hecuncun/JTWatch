package com.lhzw.bluetooth.ui.fragment.guard

import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseFragment
import com.lhzw.bluetooth.bean.CurrentDataBean
import com.lhzw.bluetooth.bean.DailyInfoDataBean
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.ConnectEvent
import com.lhzw.bluetooth.event.HideDialogEvent
import com.lhzw.bluetooth.event.RefreshGuardState
import com.lhzw.bluetooth.event.SyncDataEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.glide.GlideUtils
import com.lhzw.bluetooth.service.BleConnectService
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.uitls.XAxisValueFormatter
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fragment_guard.*
import kotlinx.android.synthetic.main.fragment_guard.cal_line_chart
import kotlinx.android.synthetic.main.fragment_guard.step_line_chart
import kotlinx.android.synthetic.main.fragment_guard.tv_cal_num
import kotlinx.android.synthetic.main.fragment_guard.tv_step_num
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import org.litepal.extension.findAll
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by heCunCun on 2021/2/5
 */
class GuardFragment:BaseFragment() {
    private var syncTime: String by Preference(Constants.SYNC_TIME, "")//最近同步时间
    private var guardEndTime: Long by Preference(Constants.GUARD_END_TIME, 0)//守护开启状态
    private var registerTime: Long? by Preference(Constants.REGISTERTIME, 0)//注册时间
    override fun useEventBus() = true
    override fun attachLayoutRes(): Int= R.layout.fragment_guard

    override fun initView(view: View) {

        if (connectState){
            iv_ble_state.setImageResource(R.drawable.ic_ble_selected)
        }else{
            iv_ble_state.setImageResource(R.drawable.ic_ble_normal)
        }

        iv_guard_start.setOnClickListener {
            if (System.currentTimeMillis()<guardEndTime){
                //守护中...
                Intent(requireContext(),GuardRunningMapActivity::class.java).apply {
                    startActivity(this)
                }
            }else{
                //守护设置页...
                Intent(requireContext(),GuardSettingActivity::class.java).apply {
                    startActivity(this)
                }
            }

        }
    }

    override fun lazyLoad() {
        tv_name.text = nickName
        GlideUtils.showRound(iv_head_photo,photoPath,R.drawable.pic_head,6)
        //计算当前天数
        val dateNow = Date(System.currentTimeMillis())
        val dateRegister =  Date(registerTime!!)
        val days = (dateNow.time - dateRegister.time)/(1000*60*60*24)
        tv_total_day.text = "登录$days 天"
        initLineChar(step_line_chart)
        initLineChar(cal_line_chart)
        getOneWeekData()

        //显示守护倒计时
        refreshGuardState()
    }
   private var countDownTimer:CountDownTimer?=null
    private fun refreshGuardState() {
        countDownTimer?.cancel()
        if (System.currentTimeMillis() < guardEndTime) {
            tv_state_tip_title.text = "守护时间剩余"
            tv_guard_state.text = "已开启"
            iv_guard_start.setImageResource(R.drawable.ic_guard_btn_open)
            //守护开启中
            countDownTimer = object : CountDownTimer(guardEndTime - System.currentTimeMillis(), 1000) {
                override fun onTick(p0: Long) {
                    tv_state_tip_content.text = DateUtils.longTimeToHMS(p0)
                }

                override fun onFinish() {
                    tv_state_tip_title.text = "点击下方红色按钮"
                    tv_state_tip_content.text = "开启守护状态"
                    tv_guard_state.text = "未开启"
                    iv_guard_start.setImageResource(R.drawable.ic_guard_btn_close)
                }

            }
            countDownTimer?.start()
        } else {
            iv_guard_start.setImageResource(R.drawable.ic_guard_btn_close)
            tv_state_tip_title.text = "点击下方红色按钮"
            tv_state_tip_content.text = "开启守护状态"
            tv_guard_state.text = "未开启"
        }

    }

    //初始化图表
    private fun initLineChar(lineChart: LineChart) {
        val dateList  = DateUtils.getDateListOfCurrentWeek()
//
//        val times = arrayOf("1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00",
//                "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "24:00")
        val times = dateList.map {
            it.split("-")[2]
        }.toTypedArray()


        lineChart.apply {
            fitScreen()
            setTouchEnabled(true)//触摸事件
            setDrawGridBackground(true)//网格线
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
                 xAxis.enableAxisLineDashedLine(10f, 10f, 0f)//  X轴线
                setDrawAxisLine(true)//是否画X轴线
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

//        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//            override fun onNothingSelected() {
//            }
//
//            override fun onValueSelected(e: Entry, h: Highlight?) {
//                Logger.e("${e.toString()}")
//                var position =e.x.toInt()
//                if (lineChart == step_line_chart ){
//                    //tv_step_chart.text=e.y.toInt().toString()
//                    //自定义方法设置圆点显示
//                    LineChartRenderer.setCirclePoints(position)
//                }else{
//                   // tv_cal_chart.text=e.y.toInt().toString()
//                    //自定义方法设置圆点显示
//                    LineChartRenderer.setCirclePoints(position)
//                }
//                lineChart.invalidate()
//            }
//
//        })

    }

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
            set1.setDrawCircleHole(false)//圆孔
            set1.setDrawCircles(false)//圆
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


    private fun getOneWeekData() {
        Thread(Runnable {
            val todayDataAndWeekdayBefore = DateUtils.todayDataAndWeekdayBefore//2020-7-7,1
            val split = todayDataAndWeekdayBefore.split(",")
            val dataString =split[0]//20-1-18
            val days=6//前面第几天是统计第一天
            val stepValues = ArrayList<Entry>()//7天步数集合
            val calValues = ArrayList<Entry>()//7天cal集合
            Logger.e("当前日期==$dataString")
            val monday = DateUtils.getDateMinusStr(dataString, days)
            Logger.e("统计第一天==$monday")
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
                    stepValues.add(Entry(i.toFloat(),currentStepNum))
                    calValues.add(Entry(i.toFloat(), currentCalNum))
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
                initLineData(step_line_chart, stepValues, R.color.green_path, R.color.blue_path)
                initLineData(cal_line_chart, calValues, R.color.color_pink_FF00FF, R.color.color_red_FF0000)

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
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideDialog(event: HideDialogEvent) {
        if (event.success){
            //  Logger.e("homeFragment 接收到数据同步成功信息")
            getOneWeekData()
        }

    }

    //蓝牙连接状态变化的event
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWatchConnectChanged(event: ConnectEvent) {
        if (event.isConnected) {//已连接
            iv_ble_state.setImageResource(R.drawable.ic_ble_selected)
        } else {//已断开显示UI布局
            iv_ble_state.setImageResource(R.drawable.ic_ble_normal)
        }
    }

    //守护状态刷新
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshGuardState(e: RefreshGuardState){
        refreshGuardState()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    companion object{
        fun getInstance():GuardFragment = GuardFragment()
    }
}