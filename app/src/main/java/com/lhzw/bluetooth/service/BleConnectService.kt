package com.lhzw.bluetooth.service

import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.ble.ExtendedBluetoothDevice
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.*
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.widget.LoadingView
import com.orhanobut.logger.Logger
import no.nordicsemi.android.support.v18.scanner.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by heCunCun on 2020/4/13
 * 蓝牙连接服务
 */
class BleConnectService : Service() {
    private var isScanning = false  //是否正在扫描
    private val SCAN_DURATION: Long = 20000//扫描持续时长10s
    private val mListValues = mutableListOf<ExtendedBluetoothDevice>()
    private var lastDeviceMacAddress: String by Preference(Constants.LAST_DEVICE_MAC_ADDRESS, "")//最后一次扫码识别的MAC
    private var lastConnectedDeviceAdress: String by Preference(Constants.LAST_CONNECTED_ADDRESS, "")//上次连接成功的设备mac
    private var connectedDeviceName: String by Preference(Constants.CONNECT_DEVICE_NAME, "")//缓存设备名称
    private var autoConnect: Boolean by Preference(Constants.AUTO_CONNECT, false)
    private var connectState: Boolean by Preference(Constants.CONNECT_STATE, false)//蓝牙连接状态
    private var bleManager: BluetoothManager? = null
    private var loadingView: LoadingView? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    companion object {
       var isConnecting = false //是否正在连接
    }

    override fun onCreate() {
        //初始配置
        Logger.e("BleConnectService  onCreate ")
        EventBus.getDefault().register(this)
        bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (autoConnect) {
            startScan()
        }
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //当Service因内存不足而被系统kill后，一段时间后内存再次空闲时，系统将会尝试重新创建此Service
        isConnecting = false
        return START_STICKY
    }

    //开始扫描蓝牙的事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun startBleScanner(event: ScanBleEvent) {
        Logger.e("BleConnectService 收到开始扫描的EventBus===开始扫描蓝牙设备...")
        startScan()
    }

    //蓝牙连接状态变化的event
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWatchConnectChanged(event: ConnectEvent) {
        if (event.isConnected) {//已连接
            Logger.e(" BleConnectService收到连接成功回调event")
            connectState = true
            autoConnect = true//将自动连接打开
            loadingView?.setLoadingTitle("同步数据中...")
        } else {//已断开显示UI布局
            mListValues.clear()
            Logger.e(" BleConnectService收到断开回调event")
            connectState = false
            if (!autoConnect) {//主动断开
                lastDeviceMacAddress = ""//清除最后一次扫码的缓存
                lastConnectedDeviceAdress=""//清除上次成功连接的设备
            }
            if (autoConnect && bleManager!!.adapter.isEnabled && !connectState) {//蓝牙处于打开状态并且可以自动连接就执行   自动连接   走扫描蓝牙流程
                startAutoScanAndConnect()
            }
        }
    }

    //手机蓝牙状态变化
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBleStateChanged(event: BleStateEvent) {
        if (event.state && autoConnect) {//打开蓝牙,并且是自动连接时才扫描
            startScan()
        }

    }

    /**
     * 同步数据成功
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideDialog(event: HideDialogEvent) {
        if (event.success) {
            showToast("数据同步成功")
        }
        isConnecting = false
        try {
            loadingView?.dismiss()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }


    private fun startScan() {
        if(!bleManager?.adapter!!.isEnabled){
            Log.e("scan","未开启手机蓝牙")
            return
        }
        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings = ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(500)
                .setUseHardwareBatchingIfSupported(false)
                .build()

        val filters = mutableListOf<ScanFilter>()//过滤器
        filters.clear()
        if (autoConnect){
            if (connectedDeviceName.isNotEmpty()){
                filters.add(ScanFilter.Builder().setDeviceName(connectedDeviceName).build())
            }

        }else{
            if(lastDeviceMacAddress.isNotEmpty()){
                filters.add(ScanFilter.Builder().setDeviceAddress(lastDeviceMacAddress).build())
            }

        }

        try {
            scanner.startScan(filters, settings, scanCallback)
            isScanning = true

            Handler().postDelayed({
                if (isScanning) {
                    stopScan()
                }
            }, SCAN_DURATION)
        } catch (e: Exception) {
            Log.e("BLE_Error", "scanner already started with given callback ...")
        }
    }

    private fun stopScan() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        try {
            scanner.stopScan(scanCallback)
            isScanning = false
            if (autoConnect && !connectState) {//重连还未连接成功
                Logger.e("未找到蓝牙,继续搜索...")
                Handler().postDelayed(Runnable {
                    startAutoScanAndConnect()
                }, scannerDelayTime)
                Logger.e("延时==$scannerDelayTime")
            } else {//扫码结束   未连接成功
                if (!connectState) {
                    autoConnect=false
                    showToast("连接失败,未发现设备:$connectedDeviceName")
                    EventBus.getDefault().post(HideDialogEvent(false))
                }
            }
        } catch (e: Exception) {
            Log.e("BLE_Error", "BT Adapter is not turned ON ...")
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            if (connectState){//已连接就不走扫描回调
                return
            }
            Log.e("Scan", "搜索设备中...size==${results.size}")
            for (result in results) {//此方法用来保存扫描到的设备
                if (result.device.name != null && result.device.name.contains("SW")) {
                    if (mListValues.size == 0) {
                        mListValues.add(ExtendedBluetoothDevice(result))
                    } else {
                        var hasExist = false
                        for (device in mListValues) {
                            if (result.device.address == device.device.address) {
                                hasExist = true
                                break
                            }
                        }
                        if (!hasExist) {
                            mListValues.add(ExtendedBluetoothDevice(result))
                        }
                    }

                }
                Logger.e("已找到筛选的周围腕表设备数量==${mListValues.size}")
            }
            if(autoConnect){
                //自动连接
                val lastList = mListValues.filter {
                    it.device.address == lastConnectedDeviceAdress
                }
                if (lastList.isNotEmpty()){//周围设备中有目标设备
                    if (!connectState) {
                        Log.e("Scan","自动连接..已找到目标蓝牙设备")
                        if (!isConnecting) {
                            Logger.e("发送连接请求...")
                            RxBus.getInstance().post("connect", BlutoothEvent(lastList[0].device, App.getActivityContext()))
                            isConnecting = true
                            //此处加入连接超时重置连接状态机制,防止发送连接指令后未成功,一直处于连接中,导致后续的指令无法继续发送
                            Handler().postDelayed({
                                if(isConnecting && !connectState){
                                    isConnecting = false
                                }

                            },20000)



                            if (loadingView == null) {
                                if (App.getActivityContext() != null) {
                                    loadingView = LoadingView(App.getActivityContext())
                                    loadingView?.setLoadingTitle("连接中...")

                                }
                            }
                            if (App.getActivityContext() != null) {
                                try {
                                    loadingView?.show()
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }else{
                            Log.e("Scan", "已发送过连接请求,15秒内不再发送连接请求")
                        }
                    }
                }else{//周围设备中没有目标设备
                    Log.e("BleConnect","autoConnect=$autoConnect,目标设备=${lastConnectedDeviceAdress},不在连接范围")
                }
            }else{
                //手动扫描
                val lastList = mListValues.filter {
                    it.device.address == lastDeviceMacAddress
                }
                if (lastList.isNotEmpty()){
                    val extendedDevice=lastList[0]
                    if (!connectState) {
                        Log.e("Scan","手动扫码找到蓝牙设备")
                        if (!isConnecting) {
                            Log.e("Scan","发送连接指令...")
                            RxBus.getInstance().post("connect", BlutoothEvent(extendedDevice.device, App.getActivityContext()))
                            isConnecting = true
                        }else{
                            Log.e("Scan","连接中...")
                        }

                    }
                }else{
                    Log.e("Scan","手动扫码未找到蓝牙设备")
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Logger.e("onScanFailed")

        }
    }

    //自动扫描并且连接
    private var autoScanner: BluetoothLeScannerCompat? = null
    private var scannerDelayTime = 2000L//默认一秒
    private fun startAutoScanAndConnect() {
        if(!bleManager?.adapter!!.isEnabled){
            Log.e("scan","未开启手机蓝牙")
            return
        }
        scannerDelayTime *= 2
        if (scannerDelayTime > 4000L) {
            scannerDelayTime = 4000L
        }
        if (autoScanner == null) {
            autoScanner = BluetoothLeScannerCompat.getScanner()
        }
        val settings = ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(500)
                .setUseHardwareBatchingIfSupported(false)
                .build()

        val filters = mutableListOf<ScanFilter>()//过滤器
        filters.add(ScanFilter.Builder().setDeviceName(connectedDeviceName).build())
        autoScanner?.startScan(filters, settings, scanCallback)

        isScanning = true
        Handler().postDelayed({
            try {
                if (isScanning) {
                    stopScan()

                }
            } catch (e: Exception) {
                Log.e("Bluetooth", "Bluetooth sync fail ...")
            }
        }, SCAN_DURATION)
    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        Logger.e("BleConnectService  onDestroy ")
        super.onDestroy()
    }
}