package com.lhzw.bluetooth.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.CompleteGuardEvent
import com.lhzw.bluetooth.event.RefreshGuardState
import com.lhzw.bluetooth.event.StartGuardEvent
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.Preference
import com.mapbox.android.core.location.*
import com.mapbox.mapboxsdk.geometry.LatLng
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by heCunCun on 2021/2/18
 */
class GuardLocationService : Service(), LocationEngineCallback<LocationEngineResult> {

    companion object{
        val TAG = "GuardLocationService"
    }
    private var guardEndTime: Long by Preference(Constants.GUARD_END_TIME, 0)//守护结束时间

    //位置更新之间的距离
    private val DEFAULT_DISPLACEMENT = 5.0f

    //位置更新的最大等待时间（以毫秒为单位）。
    private val DEFAULT_MAX_WAIT_TIME = 310000L

    //位置更新的最快间隔（以毫秒为单位）
    private val DEFAULT_FASTEST_INTERVAL = 300000L

    //位置更新之间的默认间隔
    private val DEFAULT_INTERVAL = 300000L

    private var mLocationEngine: LocationEngine? = null

    private var countDownTimer: CountDownTimer? = null
    private val mLocationEngineRequest = LocationEngineRequest.Builder(DEFAULT_INTERVAL) //要求最准确的位置
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY) //请求经过电池优化的粗略位置
            //            .setPriority(LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            //要求粗略〜10 km的准确位置
            //            .setPriority(LocationEngineRequest.PRIORITY_LOW_POWER)
            //被动位置：除非其他客户端请求位置更新，否则不会返回任何位置
            //            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
            .setDisplacement(DEFAULT_DISPLACEMENT) //设置位置更新之间的距离
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME) //设置位置更新的最大等待时间（以毫秒为单位）。
            .setFastestInterval(DEFAULT_FASTEST_INTERVAL)//设置位置更新的最快间隔（以毫秒为单位）
            .build()

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        mLocationEngine = LocationEngineProvider.getBestLocationEngine(this)
        refreshGuardState()
    }


    private fun refreshGuardState() {
        countDownTimer?.cancel()
        if (System.currentTimeMillis() < guardEndTime) {
            startGuard()
            //守护开启中
            countDownTimer = object : CountDownTimer(guardEndTime - System.currentTimeMillis(), 1000) {
                override fun onTick(p0: Long) {
                   val time =    DateUtils.longTimeToHMS(p0)
                    Log.e(TAG,"$time")
                }

                override fun onFinish() {
                    completeGuard()
                }

            }
            countDownTimer?.start()
        } else {
            completeGuard()
        }

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onSuccess(result: LocationEngineResult) {
        result.lastLocation?.run {
            Log.e(TAG, "lat= $latitude,lon =$longitude")
            val currentLatLng = LatLng(latitude, longitude)
            //保存到服务器

        }

    }

    override fun onFailure(exception: Exception) {
        Log.e(TAG, "定位失败==$exception")
    }

    private fun startGuard() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showToast("请打开定位权限")
            return
        }
        mLocationEngine?.requestLocationUpdates(mLocationEngineRequest, this, Looper.getMainLooper())
    }

    private fun completeGuard() {
        mLocationEngine?.removeLocationUpdates(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun startGuardEvent(e: RefreshGuardState){
        refreshGuardState()
        Log.e(TAG, "开始定位")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun completeGuardEvent(e:CompleteGuardEvent){
        guardEndTime = 0 //停止计时
        refreshGuardState()
        Log.e(TAG, "停止定位")
    }
}