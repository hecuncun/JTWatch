package com.lhzw.bluetooth.ui.fragment

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseFragment
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.BleStateEvent
import com.lhzw.bluetooth.event.ConnectEvent
import com.lhzw.bluetooth.event.HideDialogEvent
import com.lhzw.bluetooth.event.SyncDataEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.service.BleConnectService
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.widget.LoadingView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fragment_connect.*
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by hecuncun on 2019/11/13
 */
class ConnectFragment : BaseFragment() {
    private var connectedDeviceName: String by Preference(Constants.CONNECT_DEVICE_NAME, "")//缓存设备名称
    private var autoConnect: Boolean by Preference(Constants.AUTO_CONNECT, false)//是否自动连接
    val REQUEST_CODE = 0x333
    val PERMISS_REQUEST_CODE = 0x356
    val PERMISS_REQUEST_BLE_CODE = 0X357
    private val REQUEST_ENABLE_BLE = 101
    private var bleManager: BluetoothManager? = null
    private var state = false
    override fun useEventBus() = true

    companion object {
        fun getInstance(): ConnectFragment {
            return ConnectFragment()
        }
    }

    override fun attachLayoutRes(): Int = R.layout.fragment_connect


    override fun initView(view: View) {
        bleManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleManager?.let {
            state = it.adapter.isEnabled//拿到蓝牙状态
        }
        initBleState()
        tv_open.setOnClickListener {
            //打开蓝牙
            val intentOpen = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intentOpen, REQUEST_ENABLE_BLE)
        }

        iv_add.setOnClickListener {
            if (state) {
                //如果打开蓝牙就开始扫描
                //先判断权限
                if (!connectState) {
                    if (checkPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))) {

                        //     startScan()
                     //   jumpToScannerActivity()
                    } else {
                        requestPermission(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISS_REQUEST_BLE_CODE)
                    }
                }

            } else {
                showToast("请先打开蓝牙")
            }
        }

        tv_disconnect.setOnClickListener {
            //点击断开蓝牙,直接前端显示断开,不重新连接
            connectState = false
        //    connectedDeviceName = ""
            ll_connected_container.visibility = View.GONE
            activity?.tv_sync?.visibility = View.GONE
            //关闭自动连接
            autoConnect = false
            RxBus.getInstance().post("disconnect", "")
        }

        // 同步数据
        activity?.tv_sync?.setOnClickListener {
            if (BleConnectService.isConnecting) {
                Toast.makeText(context, "正在进行同步中，请稍后同步", Toast.LENGTH_LONG).show()
            } else {
                BleConnectService.isConnecting = true
                Toast.makeText(context, "开始同步", Toast.LENGTH_LONG).show()
                RxBus.getInstance().post("sync", SyncDataEvent("sync"))
                if (loadingView == null) {
                    loadingView = LoadingView(activity)
                }
                loadingView?.setLoadingTitle("同步数据...")
                loadingView?.show()
            }
        }
    }

    //连接页面的显示状态
    private fun initBleState() {
        tv_open.visibility = if (state) {
            View.GONE
        } else View.VISIBLE
        iv_ble_state.setImageResource(if (state) {
            R.drawable.icon_ble_open
        } else R.drawable.icon_ble_close)
        tv_state_tip.text = if (state) {
            "蓝牙已开启"
        } else "请打开蓝牙"
        //判断蓝牙连接成功状态
        if (connectState) {
            ll_connected_container.visibility = View.VISIBLE
            activity?.tv_sync?.visibility = View.VISIBLE
            tv_device_name.text = "设备名称:$connectedDeviceName"
        } else {
            ll_connected_container.visibility = View.GONE
            activity?.tv_sync?.visibility = View.GONE
        }
    }

//    private var isScanning = false  //是否正在扫描
//    private val SCAN_DURATION: Long = 30000//扫描时长10s
//
//    private fun startScan() {
//        Logger.e("开始搜索")
//        val scanner = BluetoothLeScannerCompat.getScanner()
//        val settings = ScanSettings.Builder()
//                .setLegacy(false)
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .setReportDelay(500)
//                .setUseHardwareBatchingIfSupported(false)
//                .build()
//
//        val filters = mutableListOf<ScanFilter>()//过滤器
//        try {
//            scanner.startScan(filters, settings, scanCallback)
//            isScanning = true
//
//            Handler().postDelayed({
//                if (isScanning) {
//                    stopScan()
//                }
//            }, SCAN_DURATION)
//        } catch (e: Exception) {
//            Log.e("BLE_Error", "scanner already started with given callback ...")
//        }
//    }
//
//    private fun stopScan() {
//        val scanner = BluetoothLeScannerCompat.getScanner()
//        try {
//            scanner.stopScan(scanCallback)
//            isScanning = false
//            if (autoConnect && !connectState) {//重连还未连接成功
//                Logger.e("未找到蓝牙,继续搜索...")
//                Handler().postDelayed(Runnable {
//                    startAutoScanAndConnect()
//                }, scannerDelayTime)
//                Logger.e("延时==$scannerDelayTime")
//            } else {//扫码结束   未连接成功
//                if (!connectState) {
//                    loadingView?.dismiss()
//                    showToast("连接失败,请开启腕表蓝牙,并未处于连接状态后重试")
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("BLE_Error", "BT Adapter is not turned ON ...")
//        }
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBleStateChanged(event: BleStateEvent) {
        state = event.state
        initBleState()
    }


//    private fun jumpToScannerActivity() {// Manifest.permission.VIBRATE允许访问振动设备
//        if (checkPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.VIBRATE))) {
//            val intent = Intent(activity, CaptureActivity::class.java)
//            startActivityForResult(intent, REQUEST_CODE)
//        } else {
//            requestPermission(arrayOf(Manifest.permission.CAMERA, Manifest.permission.VIBRATE), PERMISS_REQUEST_CODE)
//        }
//
//    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISS_REQUEST_CODE) {
//            val intent = Intent(activity, CaptureActivity::class.java)
//            startActivityForResult(intent, REQUEST_CODE)
//        }
//        if (requestCode == PERMISS_REQUEST_BLE_CODE) {
//            // EventBus.getDefault().post(ScanBleEvent())
//            jumpToScannerActivity()
//        }
//    }

    override fun lazyLoad() {
    }

    /**
     * 蓝牙扫描回调
     */
    private var loadingView: LoadingView? = null

    // private var requestBleConnect = false//是否请求连接蓝牙

//    private val scanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            Logger.e("onScanResult")
//        }
//
//        override fun onBatchScanResults(results: MutableList<ScanResult>) {
//            // Log.e("Tag", "result == null ${results.size}")
//
//            Logger.e("搜索设备中... ThreadName=${Thread.currentThread().name}")
//            for (result in results) {
//                if (result.device.name != null && result.device.name.contains("SW2500")) {
//                    if (mListValues.size == 0) {
//                        mListValues.add(ExtendedBluetoothDevice(result))
//                    } else {
//                        var hasExist = false
//                        for (device in mListValues) {
//                            if (result.device.address == device.device.address) {
//                                hasExist = true
//                                break
//                            }
//                        }
//                        if (!hasExist) {
//                            mListValues.add(ExtendedBluetoothDevice(result))
//                        }
//                    }
//
//                }
//                Log.e("TAG", "已找到周围腕表设备数量==${mListValues.size}")
//            }
//            if (autoConnect) {
//                val lastList = mListValues.filter {
//                    it.device.address == lastDeviceMacAddress
//                }
//
//                if (lastList.isNotEmpty()) {
//                    if (loadingView == null) {
//                        loadingView = LoadingView(activity)
//                        loadingView!!.setLoadingTitle("连接中...")
//                    }
//                    if (!connectState) {
//                        Logger.e("已找到蓝牙设备,发送连接请求...")
//                        loadingView!!.show()
//                        RxBus.getInstance().post("connect", BlutoothEvent(lastList[0].device, activity!!))
//                    }
//                }
//            } else {
//                //此处为扫码连接
//                val list = mListValues.filter {
//                    Log.e("Tag", "$macAddress   ${it.device.address}")
//                    it.device.address == macAddress
//                }
//                if (list.isNotEmpty()) {
//                    val extendedDevice = mListValues.filter {
//                        macAddress == it.device.address
//                    }[0]
//                    if (!connectState) {
//                        Logger.e("找到蓝牙设备发送连接指令...")
//                        RxBus.getInstance().post("connect", BlutoothEvent(extendedDevice.device, activity!!))
//                    }
//                } else {
//                    Logger.e("搜索目标蓝牙设备中...")
//                }
//            }
//
//
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            Logger.e("onScanFailed")
//
//        }
//    }


    //  private val mListValues = mutableListOf<ExtendedBluetoothDevice>()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            //扫描结果
//            if (data != null) {
//                data.extras?.let {
//                    if (it.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
//                        val result = it.getString(CodeUtils.RESULT_STRING)
//                        showToast("扫描结果为$result")
//                        Logger.e("result=$result")
//                        //此处进行蓝牙连接 SW2500,SW2500_D371,E3:0B:AA:DE:D3:71,00010000,6811E7ED,00010000,00000001,00010000,00010000
//                        if (result!!.split(",")[0] == "SW2500") {//如果为手表设备,扫码成功就保存设备
//                            lastDeviceMacAddress = result.split(",")[2]
//                            connectedDeviceName = result.split(",")[1]
//                            autoConnect=false //扫码成功就不自动连接,等连接成功后再设置为自动成功
//                            //下面为连接流程
//                            loadingView = LoadingView(activity)
//                            loadingView?.setLoadingTitle("连接中...")
//                            loadingView?.show()
//                            EventBus.getDefault().post(ScanBleEvent())
//                            Logger.e("发送开始扫描的EventBus")
//                        } else {
//                            showToast("无法识别的设备")
//                        }
//
//                    } else {
//                        showToast("扫描失败")
//                    }
//                }
//            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWatchConnectChanged(event: ConnectEvent) {
        if (event.isConnected) {//已连接
            loadingView?.setLoadingTitle("同步数据中...")
            ll_connected_container.visibility = View.VISIBLE
            activity?.tv_sync?.visibility = View.VISIBLE
//            if (connectedDeviceName.isEmpty()) {
//                connectedDeviceName = name
//            }
            tv_device_name.text = "设备名称:$connectedDeviceName"
            //          lastDeviceMacAddress = macAddress//保存macAddress
//            connectState = true
//            autoConnect = true//将自动连接打开
        } else {//已断开显示UI布局
            //   mListValues.clear()
            Logger.e("ConnectFragment  收到断开回调")
            //  connectState = false
            //      requestBleConnect = false
            ll_connected_container.visibility = View.GONE
            activity?.tv_sync?.visibility = View.GONE
//            if (autoConnect && bleManager!!.adapter.isEnabled && !connectState) {//蓝牙处于打开状态并且可以自动连接就执行   自动连接   走扫描流程
//
//                startAutoScanAndConnect()
//
//            }
        }
    }

    //    //自动扫描并且连接
//    private var autoScanner: BluetoothLeScannerCompat? = null
//    private var scannerDelayTime = 1000L//默认一秒
//    private fun startAutoScanAndConnect() {
//        scannerDelayTime *= 2
//        if (scannerDelayTime > 8000L) {
//            scannerDelayTime = 8000L
//        }
//        if (autoScanner == null) {
//            autoScanner = BluetoothLeScannerCompat.getScanner()
//        }
//        val settings = ScanSettings.Builder()
//                .setLegacy(false)
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .setReportDelay(500)
//                .setUseHardwareBatchingIfSupported(false)
//                .build()
//
//        val filters = mutableListOf<ScanFilter>()//过滤器
//
//        autoScanner?.startScan(filters, settings, scanCallback)
//
//        isScanning = true
//        Handler().postDelayed({
//            try {
//                if (isScanning) {
//                    stopScan()
//                }
//            } catch (e: Exception) {
//                Log.e("Bluetooth", "Bluetooth sync fail ...")
//            }
//        }, SCAN_DURATION)
//    }
    fun refleshSyncState() {
        if (connectState) {
            activity?.tv_sync?.visibility = View.VISIBLE
        } else {
            activity?.tv_sync?.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideDialog(event: HideDialogEvent) {
        loadingView?.dismiss()
    }
}