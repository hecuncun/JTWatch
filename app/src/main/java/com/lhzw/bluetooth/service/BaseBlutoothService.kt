package com.lhzw.bluetooth.service

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.PowerManager
import android.util.Log
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.*
import com.lhzw.bluetooth.ble.*
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.dfu.DfuBeanEvent
import com.lhzw.bluetooth.event.*
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.Preference
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat


/**
 *
@author：created by xtqb
@description:
@date : 2020/1/7 9:07
 *
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
abstract class BaseBlutoothService : Service(), BleManagerCallbacks {
    protected var myBleManager: BleManager? = null
    private var noFlashMap = HashMap<String, MutableList<Byte>>()
    private lateinit var readDailyBean: ReadFlashBean<DailyDataBean>
    private lateinit var readActivityBean: ReadSportAcitvityBean<SportActivityBean>
    private var readSportDetailList: MutableList<ReadDetailFlashBean> = ArrayList()
    private var readSportDetailMap = HashMap<String, HashMap<Int, MutableList<Byte>>>()
    private var readSportInfoBeanList = ArrayList<SportInfoAddrBean>()
    private val DYNAMIC_DATE = 0x01
    private val MTU_DELAY = 0x02
    protected val MTU_UPDATE_DELAY = 0x03
    protected val CONNET_UPDATE_DELAY = 0x9
    protected val DELAY_WATCH_ERROR = 0x15
    private val TIMER_STATE_REMOVE = 0x01
    private val TIMER_STATE_SEND = 0x02
    private val TIMER_STATE_ALL = 0x03
    protected var currentAddrss = ""
    private var lastConnectedDevice: String by Preference(Constants.LAST_CONNECTED_ADDRESS, "")//上次连接成功的设备mac
    private var lastDeviceMacAddress: String by Preference(Constants.LAST_DEVICE_MAC_ADDRESS, "")//缓存扫码的mac
    private var syncTime: String by Preference(Constants.SYNC_TIME, "")//最近同步时间
    private var isSyncAscending: Boolean by Preference(Constants.ISSYNCASCENDING, false)//缓存扫码的mac
    protected var acceptMsg: Boolean by Preference(Constants.ACCEPT_MSG, false)//同步数据完成后再开始接受通知
    private var ERROR = ""
    protected var mContext: Activity? = null
    protected var listMsg = mutableListOf<NotificationEvent>()//所有消息集合
    protected var isSending = false
    private var hasSports = false
    private var firm_update_time: String? by Preference(Constants.FIRM_UPDATE_TIME, "")

    //    private var progresssBar: SyncProgressBar? = null
    private var sportActivityBeanList = ArrayList<SportActivityBean>()
    protected var dfuBean: DfuBeanEvent? = null
    private var progressBarMax = 0.0f
    private var progress = 0.0f
    override fun onCreate() {
        super.onCreate()
        RxBus.getInstance().register(this)
        myBleManager = BleManager(this)
        myBleManager?.setGattCallbacks(this)
        Log.e("Tag", "start service ...")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("Tag", "start onStartCommand ...")
        acquireWakeLock()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // 有活动数据显示进度条
    override fun onActivityAddressRequestResponse(response: ByteArray?) {
        Log.e("callBackBluetooth", "onActivityAddressRequestResponse.... ${BaseUtils.byte2HexStr(response!!)}")
        response?.let {
            if (response[0].toInt() == 0x0E) {
                resetTimer(TIMER_STATE_REMOVE)
                if (sportActivityBeanList.size > 0) {
                    sportActivityBeanList.clear()
                }
                val list = CommOperation.query(BoundaryAdrrBean::class.java)
                if (list.isEmpty()) {
                    BoundaryAdrrBean.parserBoundaryAdrr(response)
                }
                // 读取活动数据
                var activities: List<SportActivityBean>
                if (isSyncAscending) {
                    activities = CommOperation.query(SportActivityBean::class.java, "daily_date", BaseUtils.getCurrentData()).filter {
                        it.current_activity_num > 0
                    }
                } else {
                    activities = CommOperation.query(SportActivityBean::class.java).filter {
                        it.current_activity_num > 0
                    }
                }
//                Log.e("parserSport", "sport_num   ${activities[0]}")
                if (activities.isNotEmpty()) {
                    val request_date = BaseUtils.longToByteArray(activities[0].request_date).toByteArray().copyOfRange(0, 6)
                    val request_mark = activities[0].current_activity_mark.toByte()
                    readActivityBean = ReadSportAcitvityBean(
                            0x0D,
                            activities,
                            request_date,
                            request_mark,
                            0,
                            0,   // 默认值是什么? 0 : 1
                            false)
                    if (readSportInfoBeanList.isNotEmpty()) {
                        readSportInfoBeanList.clear()
                    }
                    readSportActivities()
                } else {
                    // 设置手表蓝牙为低功耗
                    hasSports = false
                    resetTimer(TIMER_STATE_SEND)
                    myBleManager?.settinng_connect_parameter(false)
                    Log.e("callBackBluetooth", "settinng_connect_parameter....")
                }
            }
        }
    }

    override fun onDeviceDisconnected(device: BluetoothDevice) {
        Log.e("callBackBluetooth", "onDeviceDisconnected....")
        EventBus.getDefault().post(ConnectEvent(false))
        //断开连接后就不接收消息
        acceptMsg = false
        EventBus.getDefault().post(ProgressEvent(1.0f, 2))
    }

    override fun onAppShortMsgResponse(response: ByteArray?) {
        Logger.d("onAppShortMsgResponse")
        response?.let {
            if (it[0].toByte() == Constants.SEND_PHONE_RESPONSE_CODE) {
                if (it[1].toInt() == 0) {
                    if (listMsg.isNotEmpty()) {
                        listMsg.removeAt(0)
                    }
                    if (listMsg.size > 0) {
                        sendToPhoneData(listMsg[0])
                    } else {
                        isSending = false
                    }
                } else {
                    if (listMsg.size > 0) {
                        sendToPhoneData(listMsg[0])
                    } else {
                        isSending = false
                    }
                }
            }
        }
    }


    // 连接成功
    override fun onDeviceConnected(device: BluetoothDevice) {
//        acceptMsg = false
        Log.e("callBackBluetooth", "onDeviceConnected.... $currentAddrss  $lastConnectedDevice")
        if (currentAddrss.isNotEmpty() && !currentAddrss.equals(lastConnectedDevice)) {
            CommOperation.deleteAll(WatchInfoBean::class.java)
            //CommOperation.deleteAll(SportInfoBean::class.java)
            CommOperation.deleteAll(BoundaryAdrrBean::class.java)
//            CommOperation.deleteAll(ClimbingSportBean::class.java)
            CommOperation.deleteAll(CurrentDataBean::class.java)
            CommOperation.deleteAll(DailyDataBean::class.java)
            CommOperation.deleteAll(DailyInfoDataBean::class.java)
//            CommOperation.deleteAll(FlatSportBean::class.java)
            CommOperation.deleteAll(SportActivityBean::class.java)
//            CommOperation.deleteAll(SportInfoAddrBean::class.java)
//            lastConnectedDevice = currentAddrss
            isSyncAscending = false
        }
        EventBus.getDefault().post(ConnectEvent(true))
        //1.连接成功  特征使能
        // myBleManager?.notification_enable()
//        Observable.just(true).delay(3, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe {
//            //2.更新连接参数
//            Log.e("Tag", "update ....")
//            myBleManager?.connection_update(it)
//        }
//        Handler().postDelayed(Runnable {
//            Log.e("Tag", "update ....")
//            myBleManager?.connection_update(true)
//         //   myBleManager?.mtu_update()
//        },500)


        /*
         //3.更新mtu大小
         myBleManager?.mtu_update()
         //4.更新手表时间
         myBleManager?.watch_time_update()
         //5.读取手表信息
         myBleManager?.device_info()
         //6.更新用户个人信息  TODO:传值更新个人信息
         myBleManager?.personal_info_update()
         //7.同步日常数据onDailyDataReqest 读取日常数据,判断运动天数是否大于0,大于0再去读NorFlash(日常数据地址  日常数据大小 )
         myBleManager?.daily_data_request()

         //8.发送更新蓝牙连接参数命令，将蓝牙连接间隙修改为慢速连接,
         // 注意：如果连接流程中有短消息需要推送，将短消息推迟到整个连接流程结束再推送给手表。
         myBleManager?.connection_update(false)
         Log.e("Tag", "connect success ...")
         */
    }

    override fun onBondingRequired(device: BluetoothDevice) {

    }

    override fun onDeviceNotSupported(device: BluetoothDevice) {

    }

    override fun onPersonalInfoReadResponse(response: ByteArray?) {

    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {

    }

    override fun onLinkLossOccurred(device: BluetoothDevice) {

    }

    override fun onBonded(device: BluetoothDevice) {

    }

    override fun onSettingConnectParameter(response: ByteArray?) {
        // 设置手表为低功率状态
        Log.e("callBackBluetooth", "onSettingConnectParameter....")
        syncTime = "${DateUtils.longToString(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")}"
        response(response, Constants.CONNECT_RESPONSE_CODE) {
            resetTimer(TIMER_STATE_REMOVE)
            // 刷新界面
            RxBus.getInstance().post("reflesh", "")
            //开始接受消息提醒
            lastConnectedDevice = lastDeviceMacAddress
            BleConnectService.isConnecting = false
            App.setSynState(true)
            if ("" == firm_update_time) { // 说明第一次成功连接腕表
                val sdf = SimpleDateFormat("yyyy年MM月dd日更新")
                firm_update_time = sdf.format(System.currentTimeMillis())
            }
            acceptMsg = true
            if (hasSports) {
                sportActivityBeanList.forEach {
                    var value = ContentValues()
                    value.put("request_date", it.request_date)
                    value.put("current_activity_mark", it.current_activity_mark)
                    CommOperation.update(SportActivityBean::class.java, value, it.id)
                }
                if (!isSyncAscending) {
                    isSyncAscending = true
                }
                // 要保证第一次同步完成后才能进入累加获取数据
                sportActivityBeanList.clear()
                EventBus.getDefault().post(ProgressEvent(1.0f, 2))
            } else {
                //开始连接进入进度条,连接并初始化成功后再发成功
            }
            EventBus.getDefault().post(HideDialogEvent(true))
        }
    }

    override fun onMtuUpdateResponse(response: ByteArray?) {
        Log.e("callBackBluetooth", "onMtuUpdateResponse....")
        mHandler.removeMessages(MTU_DELAY)
        requestTimes = 0
        response(response, Constants.MTU_RESPONSE_CODE) {
            myBleManager?.watch_time_update()
            Log.e("callBackBluetooth", "watch_time_update....")
//            myBleManager?.device_info()
        }
    }

    // 蓝牙本页入口
    override fun onConnectionUpdateResponse(response: ByteArray?) {
        //更新连接成功后    更新mtu
//        Log.e("Watch", "onConnectionUpdateResponse .... ${BaseUtils.byte2HexStr(response!!)}")
        Log.e("callBackBluetooth", "onConnectionUpdateResponse....  $isSyncAscending")
        response(response, Constants.CONNECT_RESPONSE_CODE) {
//            myBleManager?.mtu_update()
            acceptMsg = false
            mHandler.sendEmptyMessage(MTU_DELAY)
            App.setSynState(false)
        }
    }

    override fun onDeviceInfoResponse(response: ByteArray?) {
        response?.let {
            if (response[0].toInt() == 0x02) {
//                Log.e("Watch", "onDeviceInfoResponse .... ${BaseUtils.byte2HexStr(response!!)}")
                Log.e("callBackBluetooth", "onDeviceInfoResponse....")
                var bean = WatchInfoBean.createBean(response)
                bean?.let {
                    var list = CommOperation.query(WatchInfoBean::class.java)
                    list?.let {
                        CommOperation.deleteAll(WatchInfoBean::class.java)
                    }
                    CommOperation.insert(it)
                }
                myBleManager?.personal_info_update(PersonalInfoBean.createBytes())  // 更新个人数到手表
                Log.e("callBackBluetooth", "personal_info_update....")
            }
        }
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {

    }

    override fun onDeviceReady(device: BluetoothDevice) {

    }

    override fun onWatchTimeUpdateResponse(response: ByteArray?) {
//        Log.e("Watch", "onWatchTimeUpdateResponse .... ${BaseUtils.byte2HexStr(response!!)}")
        Log.e("callBackBluetooth", "onWatchTimeUpdateResponse....")
        response(response, Constants.UPDATE_TIME_RESPONSE_CODE) {
            myBleManager?.device_info()
            Log.e("callBackBluetooth", "device_info....")
        }
    }

    override fun onPersonalInfoUpdateResponse(response: ByteArray?) {
//        Log.e("Tag", "onPersonalInfoUpdateResponse ...  ${BaseUtils.byte2HexStr(response!!)}")
        Log.e("callBackBluetooth", "onPersonalInfoUpdateResponse....")
        //更新个人信息成功就开始同步动态数据
        response(response, Constants.UPDATE_PERSON_INFO_RESPONSE_CODE) {
//            myBleManager?.current_data_update()
            mHandler.sendEmptyMessageDelayed(DYNAMIC_DATE, 3000)
        }
    }

    // 获取活动数据
    override fun onSportsParamReadResponse(response: ByteArray?, ID: String) {
        response?.let {
            Log.e("readSport", "onSportsParamReadResponse ... ${BaseUtils.byte2HexStr(response)}")
            Log.e("callBackBluetooth", "onSportsParamReadResponse.... $ID")
            if (response[0].toInt() == 0x0D && Constants.ACTIVITIES.contains(response[1].toInt() and 0xFF)) {
                resetTimer(TIMER_STATE_REMOVE)
                //解析当前活动
                SportInfoAddrBean.parserSportInfoAddr(response, ID) { data, mark, bean ->
//                    Log.e("Sportmark", "mark = $mark")
                    readActivityBean.request_date = data
                    readActivityBean.request_mark = mark
                    readSportInfoBeanList.add(bean)
                    readNextActivity()
                }
            } else {
                // 设置手表蓝牙为低功耗
                resetTimer(TIMER_STATE_SEND)
                myBleManager?.settinng_connect_parameter(false)
                Log.e("callBackBluetooth", "settinng_connect_parameter.... ")
//                Toast.makeText(App.context, "获取信息有误.同步数据结束，请求指令：$ERROR", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSportsParamUpdateResponse(response: ByteArray?) {

    }

    override fun onDailyDataRequestResponse(response: ByteArray?) {
//        Log.e("Tag", "onDailyDataRequestResponse ...  ${BaseUtils.byte2HexStr(response!!)}")
        Log.e("callBackBluetooth", "onDailyDataRequestResponse.... ${BaseUtils.byte2HexStr(response!!)}")
        response?.let {
            if (response[0].toInt() == 0x0C) {
                resetTimer(TIMER_STATE_REMOVE)
                DailyDataBean.parserDailyData(response, isSyncAscending) { datas ->
                    if (datas.size == 0) {
                        // 设置手表蓝牙为低功耗
                        resetTimer(TIMER_STATE_SEND)
                        myBleManager?.settinng_connect_parameter(false)
                    } else {
                        if (datas.size > 0) {
                            noFlashMap.clear()
                        }
                        // 从noFlash获取数据信息
                        var len = Constants.MTU_MAX
                        var isOver = false
                        if (datas[0].data_len < Constants.MTU_MAX) {
                            len = datas[0].data_len
                            if (datas.size == 1) {
                                isOver = true
                            }
                        }
                        readDailyBean = ReadFlashBean(datas, 0, 0x0C, datas[0].start_addr, len, 1, isOver)
                        readDailyNoFlash()
                    }
                }
            }
        }
    }

    override fun onSportDetailInfoResponse(response: ByteArray?, request_code: Byte, type: Int, ID: String) {
//        Log.e("SportDetail", "readSportDetailData  type : $type  ... ${BaseUtils.byte2HexStr(response!!)}")
        Log.e("callBackBluetooth", "onSportDetailInfoResponse.... $ID")
        if (request_code.toInt() == 0x0D) {
            response?.let {
                resetTimer(TIMER_STATE_REMOVE)
                if (response.size > 11) {
                    progress++
                    EventBus.getDefault().post(ProgressEvent(progress / progressBarMax, 0))

                    if (readSportDetailMap.get(ID) == null) {
                        val detail = HashMap<Int, MutableList<Byte>>()
                        readSportDetailMap.put(ID, detail)
                    }
                    val map = readSportDetailMap.get(ID)
                    if (map!![type] == null) {
                        val list = ArrayList<Byte>()
                        val tmp = it.toList()
                        list.addAll(tmp.subList(11, tmp.size))
                        map.put(type, list)
//                        Log.e("sportdetail", "type $type  ${BaseUtils.byte2HexStr(tmp.subList(11, tmp.size).toByteArray())}")
                    } else {
                        val tmp = it.toList()
                        map[type]?.addAll(tmp.subList(11, tmp.size))

                    }
                    readSportDetailBean()
                }
            }
        }
    }

    override fun onNorFlashEraseResponse(response: ByteArray?) {

    }

    override fun onServicesDiscovered(device: BluetoothDevice, optionalServicesFound: Boolean) {

    }

    override fun onBondingFailed(device: BluetoothDevice) {

    }

    override fun onNorFlashReadResponse(response: ByteArray?, ID: String) {
//        Log.e("Tag", "onNorFlashReadResponse ...  ${BaseUtils.byte2HexStr(response!!)}   ${response.size}")
        Log.e("callBackBluetooth", "onNorFlashReadResponse.... $ID")
        response?.let {
            if (response[0].toInt() == 0x04 && response[1].toInt() == 0x0C) {
                resetTimer(TIMER_STATE_REMOVE)
//                Log.e("dailyinfo", "$ID : ${BaseUtils.byte2HexStr(response)}")
                if (noFlashMap[ID] == null) {
                    val list = ArrayList<Byte>()
                    val tmp = it.toList()
                    list.addAll(tmp.subList(11, tmp.size))
                    noFlashMap[ID] = list
                } else {
                    val tmp = it.toList()
                    noFlashMap[ID]!!.addAll(tmp.subList(11, tmp.size))
                }
                if (readDailyBean.isOver) {
                    Thread {
                        DailyInfoDataBean.parserDailyInfoBean(noFlashMap) {
                            resetTimer(TIMER_STATE_SEND)
                            myBleManager?.read_boundary_address()
                            Log.e("callBackBluetooth", "read_boundary_address....")
                        }
                    }.start()
                } else {
                    readNextAddr()
                }
            }
        }
    }

    // 读取下一个地址数据
    private fun readNextAddr() {
        when (readDailyBean.request_code) {
            Constants.UPDATE_DAILY_INFO_RESPONSE_CODE -> {
                val bean = readDailyBean.list[readDailyBean.index]
                if (bean.data_len > (readDailyBean.counter_mtu + 1) * Constants.MTU_MAX) {
                    readDailyBean.counter_mtu++
                    readDailyBean.current_addr += Constants.MTU_MAX
                    readDailyBean.read_len = Constants.MTU_MAX
                    readDailyNoFlash()
                } else {
                    readDailyBean.current_addr += Constants.MTU_MAX
                    readDailyBean.read_len = bean.data_len - readDailyBean.counter_mtu * Constants.MTU_MAX
                    readDailyNoFlash()
                    if (readDailyBean.list.size == readDailyBean.index + 1) {
                        readDailyBean.isOver = true
                    } else {
                        readDailyBean.index++
                        readDailyBean.current_addr = readDailyBean.list[readDailyBean.index].start_addr - Constants.MTU_MAX
                        readDailyBean.counter_mtu = 0
                        readDailyBean.isOver = false
                    }
                }
            }
        }
    }

    override fun onNorFlashWriteResponse(response: ByteArray?) {

    }

    override fun onError(device: BluetoothDevice, message: String, errorCode: Int) {
        Log.e("Bluetooth", "${device}    ${message}     ${errorCode}")
    }

    override fun onWatchDataResponse(response: ByteArray?) {
        // 获取数据

    }

    override fun onCurrentDataUpdate(response: ByteArray?) {
        //  解析当前数据
        response?.let {
//            Log.e("currentData", "currentdata  ${BaseUtils.byte2HexStr(response)}")
            Log.e("callBackBluetooth", "onCurrentDataUpdate....")
            if (it[0] == Constants.CURRENT_DATA_UPDATE_RESPONSE_CODE && it.size == 25) {
                mHandler.removeMessages(DYNAMIC_DATE)
                requestTimes = 0
                CommOperation.deleteAll(CurrentDataBean::class.java)
                val bean = CurrentDataBean.createBean(response)
                bean?.let {
                    CommOperation.insert(it)
                }
                //保存到数据库,并刷新页面
                Logger.e("当前步数==${bean!!.dailyStepNumTotal + bean.sportStepNumTotal},当前cal=${bean.dailyCalTotal + bean.sportCalTotal}")
                EventBus.getDefault().post(RefreshTargetStepsEvent())
                //动态数据后,TODO 请求日常数据
                val list = CommOperation.query(SportActivityBean::class.java, "daily_date", BaseUtils.getCurrentData())
                if (list.isNotEmpty()) {
                    val date = BaseUtils.longToByteArray(list[0].request_date).toByteArray()
//                    Log.e("parserDaily", "${BaseUtils.byte2HexStr(date)}   ${list[0].current_activity_num}")
                    val content = byteArrayOf(
                            0x0C, date[0], date[1], date[2], date[3], date[4], date[5], list[0].current_activity_mark.toByte()
                    )
                    myBleManager?.daily_data_request(content)
                } else {
                    myBleManager?.daily_data_request(byteArrayOf(0x0C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
                }
                resetTimer(TIMER_STATE_SEND)
                Log.e("callBackBluetooth", "daily_data_request....")
            }
        }
    }

    // 读取noflash中日常数据
    private fun readDailyNoFlash() {
        var bean = readDailyBean.list[readDailyBean.index]
        Thread {
            val content = ArrayList<Byte>()
            content.add(0x04)
            content.add(bean.response.toByte())
            content.addAll(BaseUtils.intToByteArray(readDailyBean.current_addr))
            content.add((readDailyBean.read_len and 0xff).toByte())
//            Log.e("Daily", "${bean.sport_date}  ${BaseUtils.byte2HexStr(content.toByteArray())}")
            resetTimer(TIMER_STATE_SEND)
            myBleManager?.norFlash_read(content.toByteArray(), bean.sport_date)
            Log.e("callBackBluetooth", "norFlash_read....")
            content.clear()
        }.start()
    }

    /**
     * 读取日常活动数据
     */
    private fun readSportActivities() {
        if (readActivityBean.isOver) {
            Log.e("readSportActivities", "read sport activity over ...")
            val bean = readActivityBean.list[readActivityBean.bean_index]
            val list = CommOperation.query(SportActivityBean::class.java, "daily_date", bean.daily_date)
            list[0].request_date = BaseUtils.byteToLong(readActivityBean.request_date.toList())
            list[0].current_activity_mark = readActivityBean.request_mark.toInt()
            sportActivityBeanList.add(list[0])
            init_sport_detail_addr()
        } else {
            val bean = readActivityBean.list[readActivityBean.bean_index]
            val activities_addr = BaseUtils.intToByteArray(bean.activities_addr)
            val content = ArrayList<Byte>()
            content.add(readActivityBean.request_code)
            content.addAll(readActivityBean.request_date.toList())
            content.add(readActivityBean.request_mark)
            content.addAll(activities_addr)
            var value = BaseUtils.byte2HexStr(content.toByteArray())!!
            ERROR += "\n日期 ${value.substring(3, 20)}\n活动序号 ：${value.substring(21, 23)}\n地址：${value.substring(24, 35)}"
//            Log.e("readSport", "$bean.daily_date  $value}")
            resetTimer(TIMER_STATE_SEND)
            myBleManager?.sports_param_read(content.toByteArray(), bean.daily_date + "-" + BaseUtils.byte2HexStr(byteArrayOf(readActivityBean.request_mark)))
            Log.e("callBackBluetooth", "sports_param_read....")
            content.clear()
        }
    }

    /**
     *
     * 读取下一个活动  判断 bean_index activity_index
     *
     */
    private fun readNextActivity() {
        val bean = readActivityBean.list[readActivityBean.bean_index]
        if (bean.current_activity_num == readActivityBean.activity_index + 1) {
            if (readActivityBean.list.size == readActivityBean.bean_index + 1) {
                readActivityBean.isOver = true
            } else {
                // 更新bean
                val list = CommOperation.query(SportActivityBean::class.java, "daily_date", bean.daily_date)
                list[0].request_date = BaseUtils.byteToLong(readActivityBean.request_date.toList())
                list[0].current_activity_mark = readActivityBean.request_mark.toInt()
                sportActivityBeanList.add(list[0])
                // 请求下一个bean 数据
                readActivityBean.bean_index++
                readActivityBean.activity_index = 0
                readActivityBean.request_date = BaseUtils.longToByteArray(readActivityBean.list[readActivityBean.bean_index].request_date).toByteArray().copyOfRange(0, 6)
                readActivityBean.request_mark = readActivityBean.list[readActivityBean.bean_index].current_activity_mark.toByte()
            }
        } else {
            readActivityBean.activity_index++
        }
        readSportActivities()
    }

    // 初始化读取的地址信息
    private fun init_sport_detail_addr() {
        if (readSportDetailList.isNotEmpty()) {
            readSportDetailList.clear()
        }
        // 根据地址读取数据
        val boundaries = CommOperation.query(BoundaryAdrrBean::class.java)
        readSportInfoBeanList?.forEach {
            for (type in 0 until Constants.TOTAL) {
                when (type) {
                    Constants.STEP -> {
                        if (it.step_start_addr > it.step_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.STEP, it.step_start_addr, boundaries[0].record_steps_end)
                            addDetailBean(it.sport_detail_mark, Constants.STEP, boundaries[0].record_steps_start, it.step_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.STEP, it.step_start_addr, it.step_end_addr)
                        }
                    }
                    Constants.HEART_RATE -> {
                        if (it.heart_rate_start_addr > it.heart_rate_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.HEART_RATE, it.heart_rate_start_addr, boundaries[0].heart_rate_end)
                            addDetailBean(it.sport_detail_mark, Constants.HEART_RATE, boundaries[0].heart_rate_start, it.heart_rate_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.HEART_RATE, it.heart_rate_start_addr, it.heart_rate_end_addr)
                        }
                    }
                    Constants.AIR_PRESSURE -> {
                        if (it.air_pressure_start_addr > it.air_pressure_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.AIR_PRESSURE, it.air_pressure_start_addr, boundaries[0].air_pressure_end)
                            addDetailBean(it.sport_detail_mark, Constants.AIR_PRESSURE, boundaries[0].air_pressure_start, it.air_pressure_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.AIR_PRESSURE, it.air_pressure_start_addr, it.air_pressure_end_addr)
                        }
                    }
                    Constants.GPS -> {
                        if (it.gps_start_addr > it.gps_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.GPS, it.gps_start_addr, boundaries[0].gps_end)
                            addDetailBean(it.sport_detail_mark, Constants.GPS, boundaries[0].gps_start, it.gps_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.GPS, it.gps_start_addr, it.gps_end_addr)
                        }
                    }
                    Constants.DISTANCE -> {
                        if (it.distance_start_addr > it.distance_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.DISTANCE, it.distance_start_addr, boundaries[0].distance_end)
                            addDetailBean(it.sport_detail_mark, Constants.DISTANCE, boundaries[0].distance_start, it.distance_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.DISTANCE, it.distance_start_addr, it.distance_end_addr)
                        }
                    }
                    Constants.CALORIE -> {
                        if (it.calorie_start_addr > it.calorie_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.CALORIE, it.calorie_start_addr, boundaries[0].calorie_end)
                            addDetailBean(it.sport_detail_mark, Constants.CALORIE, boundaries[0].calorie_start, it.calorie_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.CALORIE, it.calorie_start_addr, it.calorie_end_addr)
                        }
                    }
                    Constants.SPEED -> {
                        if (it.speed_start_addr > it.speed_end_addr) {
                            addDetailBean(it.sport_detail_mark, Constants.SPEED, it.speed_start_addr, boundaries[0].speed_end)
                            addDetailBean(it.sport_detail_mark, Constants.SPEED, boundaries[0].speed_start, it.speed_end_addr)
                        } else {
                            addDetailBean(it.sport_detail_mark, Constants.SPEED, it.speed_start_addr, it.speed_end_addr)
                        }
                    }
                }
            }
        }
        if (readSportInfoBeanList.isNotEmpty()) {
            readSportInfoBeanList.clear()
        }
        // 计算显示进度条最大值
        progressBarMax = 0.0f
        readSportDetailList.forEach {
            var len = it.read_len
            while (len > 0) {
                len -= Constants.MTU_MAX
                progressBarMax++
            }
        }
        if (progressBarMax > 0) {
            hasSports = true
            EventBus.getDefault().post(ProgressEvent(0.0f, 0))
            readSportDetailBean()
        }
    }

    private fun readSportDetailBean() {
        if (readSportDetailList.size > 0) {
            val bean = readSportDetailList[readSportDetailList.size - 1]
            var content = ArrayList<Byte>()
            content.add(0X04)
            content.add(0x0D)
            content.addAll(BaseUtils.intToByteArray(bean.current_addr))
            var len = 0;
            if (bean.read_len > Constants.MTU_MAX) {
                len = Constants.MTU_MAX
                bean.current_addr += Constants.MTU_MAX
                bean.read_len -= Constants.MTU_MAX
            } else {
                len = bean.read_len
                readSportDetailList.removeAt(readSportDetailList.size - 1)
            }
            content.add((len and 0xFF).toByte())
//            Log.e("SportDetail", "sendCMD:  ${BaseUtils.byte2HexStr(content.toByteArray())}")
            resetTimer(TIMER_STATE_SEND)
            myBleManager?.sport_detail_info_request(content.toByteArray(), 0x0D, bean.data_type, bean.sport_detail_mark)
            Log.e("callBackBluetooth", "sport_detail_info_request....  ${bean.sport_detail_mark}")
        } else {
//            Log.e("Tag", "parser sport detail addr over ...")
            // 进行数据解析
            progressBarMax = 0.0f
            readSportDetailMap.forEach { (mark, data) ->
                data.forEach { (type, content) ->
                    val read_len = content.size
                    when (type) {
                        // 除了Gps 4字节数据  活动步数 单位步 1分钟 活动距离 单位 cm  热量 单位 卡 一分钟 速度 单位 m/s 一分钟
                        Constants.STEP, Constants.DISTANCE, Constants.CALORIE -> {
                            if (read_len > 0) {
                                progressBarMax += 1
                            }
                        }
                        // 一个字节  一分钟  心率
                        Constants.HEART_RATE -> {
                            if (read_len > 0) {
                                progressBarMax += 1
                            }
                        }
                        // 4字节 气压 单位帕  高度4字节浮点数  高度米 5分钟
                        Constants.AIR_PRESSURE -> {
                            if (read_len > 0) {
                                progressBarMax += 1
                            }
                        }
                        // 8个字节数据 经纬度各占四个字节，带符号整形数据 然后在除 1000000  高精度 1s 低电量 1s或者5s
                        Constants.GPS -> {
                            if (read_len > 0) {
                                progressBarMax += 1
                            }
                        }
                        Constants.SPEED -> {
                            if (read_len > 0) {
                                progressBarMax += 1
                            }
                        }
                    }
                }
            }
            progress = 0.0f
            EventBus.getDefault().post(ProgressEvent(progress, 1))
            Thread {
                SportDetailInfobean.parserSportDetailInfo(readSportDetailMap) {
                    progress++
                    EventBus.getDefault().post(ProgressEvent(progress / progressBarMax, 1))
                }
                readSportDetailMap.clear()
                // 设置手表蓝牙为低功耗
                resetTimer(TIMER_STATE_SEND)
                myBleManager?.settinng_connect_parameter(false)
                Log.e("callBackBluetooth", "settinng_connect_parameter....")
            }.start()

        }
    }

    private fun addDetailBean(mark: String, type: Int, start_addr: Int, end_addr: Int) {
        if (end_addr - start_addr > 0) {
            val bean = ReadDetailFlashBean(0x0D, mark, type, start_addr, end_addr - start_addr)
            readSportDetailList.add(bean)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // 杀死线程清理数据
        EventBus.getDefault().post(ProgressEvent(0.0f, 2))
        Logger.e("杀死进程")
        onClear()
        Log.e("BluetoothWatch", "onTaskRemoved ...");
    }

    protected fun response(bytes: ByteArray?, RESPONSE: Byte, body: () -> Unit) {
        bytes?.let {
            if (it[0].toByte() == RESPONSE) {
                if (it[1].toInt() == 0) {
                    body()
                }
            }
        }
    }

    protected var requestTimes: Int = 0  //发送次数
    protected var mHandler = object : Handler() {
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                DYNAMIC_DATE -> {
                    Log.e("callBackBluetooth", "current_data_update....")
                    requestTimes++
                    if (requestTimes < 4) {
                        myBleManager?.current_data_update()
                        sendEmptyMessageDelayed(DYNAMIC_DATE, 3000)
                    } else {//动态数据同步失败
                        removeMessages(DYNAMIC_DATE)
                        requestTimes = 0
                        showToast("当前数据同步失败")
                        EventBus.getDefault().post(HideDialogEvent(false))
                    }

                }
                MTU_DELAY -> {
                    Log.e("callBackBluetooth", "mtu_update....")
                    requestTimes++
                    if (requestTimes < 4) {
                        myBleManager?.mtu_update()
                        sendEmptyMessageDelayed(MTU_DELAY, 3000)
                    } else {
                        removeMessages(MTU_DELAY)
                        requestTimes = 0
                        showToast("MTU同步失败")
                        EventBus.getDefault().post(HideDialogEvent(false))
                    }
                }
                MTU_UPDATE_DELAY -> {
                    requestTimes++
                    if (requestTimes < 4) {
                        myBleManager?._mtu_update()
                        sendEmptyMessageDelayed(MTU_UPDATE_DELAY, 3000)
                    } else {
                        removeMessages(MTU_UPDATE_DELAY)
                        requestTimes = 0
//                        showToast("MTU同步失败")
                        EventBus.getDefault().post(HideDialogEvent(false))
                    }
                }
                CONNET_UPDATE_DELAY -> {
                    requestTimes++
                    if (requestTimes < 4) {
                        myBleManager?.connection_update()
                        sendEmptyMessageDelayed(CONNET_UPDATE_DELAY, 8000)
                    } else {
                        removeMessages(CONNET_UPDATE_DELAY)
                        requestTimes = 0
                        // 解析失败
                        RxBus.getInstance().post("onupdateprogress", "-2")
//                        showToast("MTU同步失败")
                    }
                }
                DELAY_WATCH_ERROR -> {
                    showToast("试试------------------------------------------------------------");
                    removeMessages(DELAY_WATCH_ERROR)
                    EventBus.getDefault().post(ProgressEvent(0.0f, 4))
                }
            }
        }
    }

    /**
     *  state 是否是结束位置
     */
    protected fun resetTimer(state: Int) {
        when (state) {
            TIMER_STATE_REMOVE -> {
                mHandler.removeMessages(DELAY_WATCH_ERROR)
            }
            TIMER_STATE_SEND -> {
                mHandler.sendEmptyMessageDelayed(DELAY_WATCH_ERROR, 5000)
            }
            TIMER_STATE_ALL -> {
                mHandler.removeMessages(DELAY_WATCH_ERROR)
                mHandler.sendEmptyMessageDelayed(DELAY_WATCH_ERROR, 5000)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Tag", "start service onDestroy ...")
        RxBus.getInstance().unregister(this)
        Logger.e("service onDestroy...")
        releaseWakeLock()
        onClear()
    }

    open abstract fun sendToPhoneData(event: NotificationEvent)

    // 清理线程清理数据接口
    open abstract fun onClear()

    private var wakeLock: PowerManager.WakeLock? = null

    /**
     * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
     */
    private fun acquireWakeLock() {
        if (null == wakeLock) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    or PowerManager.ON_AFTER_RELEASE, javaClass
                    .canonicalName)
            if (null != wakeLock) {
                Log.i("WakeLock", "call acquireWakeLock")
                wakeLock!!.acquire()
            }
        }
    }

    // 释放设备电源锁
    private fun releaseWakeLock() {
        if (null != wakeLock && wakeLock!!.isHeld) {
            Log.i("WakeLock", "call releaseWakeLock")
            wakeLock!!.release()
            wakeLock = null
        }
    }

    // 显示进度条
    /*
    private fun showProgressBar() {
        //开始连接进入进度条,连接并初始化成功后再发成功
        hasSports = true
        EventBus.getDefault().post(HideDialogEvent(true))
        if (mContext != null && !mContext!!.isFinishing) {
            if (progresssBar == null) {
                progresssBar = SyncProgressBar(mContext!!)
            }
            progresssBar?.show()
            progresssBar?.window?.setGravity(Gravity.BOTTOM)
            var lp = progresssBar?.getWindow()?.attributes
            lp?.y = 110
            progresssBar?.window?.attributes = lp
        }
    }

    private fun cancelProgressBar() {
        BaseUtils.ifNotNull(mContext, progresssBar) { it, p ->
            if (!it.isFinishing) {
                p.cancel()
                progresssBar = null
            }
        }
    }

     */
}

