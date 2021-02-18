package com.lhzw.bluetooth.ui.fragment.guard

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseMapActivity
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.RefreshGuardState
import com.lhzw.bluetooth.ext.showToast
import com.lhzw.bluetooth.uitls.Preference
import com.mapbox.android.core.location.*
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_guard_setting.*
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * Created by heCunCun on 2021/2/6
 */
class GuardSettingActivity : BaseMapActivity(),LocationEngineCallback<LocationEngineResult> {
    private var guardTime = 60//分钟
    private var guardEndTime: Long by Preference(Constants.GUARD_END_TIME, 0)//守护结束时间
    private var mapBoxMap:MapboxMap?=null

    //位置更新之间的距离
    private val DEFAULT_DISPLACEMENT = 5.0f

    //位置更新的最大等待时间（以毫秒为单位）。
    private val DEFAULT_MAX_WAIT_TIME = 10000L

    //位置更新的最快间隔（以毫秒为单位）
    private val DEFAULT_FASTEST_INTERVAL = 5000L

    //位置更新之间的默认间隔
    private val DEFAULT_INTERVAL = 15000L

    private var mLocationEngine: LocationEngine? = null

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

    override fun onResume() {
        super.onResume()
        //请求位置信息
        mLocationEngine = LocationEngineProvider.getBestLocationEngine(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0);
            return
        }
        mLocationEngine?.requestLocationUpdates(mLocationEngineRequest, this, Looper.getMainLooper())
    }

    override fun onPause() {
        super.onPause()
        //结束请求位置
            mLocationEngine?.removeLocationUpdates(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0);
            return
        }
        mLocationEngine?.requestLocationUpdates(mLocationEngineRequest, this, Looper.getMainLooper())
    }


    override fun getMapView(): MapView = mapView

    override fun attachLayoutRes(): Int = R.layout.activity_guard_setting

    override fun initData() {
    }

    override fun initView() {
        mapView.getMapAsync { mapBox ->
            mapBoxMap = mapBox
            mapBox.setStyle(Style.Builder().fromUrl("asset://mapbox.json"))
            mapBox.uiSettings.apply {
                isCompassEnabled = false
                isLogoEnabled = false
                isAttributionEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isDeselectMarkersOnTap = true
            }
        }


    }

    private var scheduledExecutor: ScheduledExecutorService? = null

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                R.id.iv_reduce -> {
                    guardTime--
                    if (guardTime < 1) {
                        guardTime = 1
                        showToast("已到达最少守护时长")
                    }
                    tv_total_time.text = guardTime.toString()
                }//减小操作
                R.id.iv_add -> {
                    guardTime++
                    if (guardTime > 1440) {
                        guardTime = 1440
                        showToast("已到达最大守护时长")
                    }
                    tv_total_time.text = guardTime.toString()
                } //增大操作
            }
        }
    }

    private fun stopAddOrSubtract() {
        if (scheduledExecutor != null) {
            scheduledExecutor!!.shutdownNow();
            scheduledExecutor = null
        }
    }

    private fun updateAddOrSubtract(id: Int) {
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
        scheduledExecutor!!.scheduleWithFixedDelay(Runnable {
            val msg = Message()
            msg.what = id
            handler.sendMessage(msg)
        }, 0, 100, TimeUnit.MILLISECONDS)
    }

    override fun initListener() {
        iv_reduce.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    updateAddOrSubtract(v.id)//手指按下时触发不停的发送消息
                } else if (event.action == MotionEvent.ACTION_UP) {
                    stopAddOrSubtract() //手指抬起时停止发送
                }
                return true
            }

        })

        iv_add.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    updateAddOrSubtract(v.id)//手指按下时触发不停的发送消息
                } else if (event.action == MotionEvent.ACTION_UP) {
                    stopAddOrSubtract() //手指抬起时停止发送
                }
                return true
            }

        })

        iv_start.setOnClickListener {
            //开始倒计时
            finish()
            //设置记录开始守护的时间
            guardEndTime = System.currentTimeMillis() + guardTime * 60 * 1000
            EventBus.getDefault().post(RefreshGuardState())

        }
        iv_back.setOnClickListener {
            finish()
        }
    }

    private var centerLatLon: LatLng?=null
    private var cerMarker: Marker?=null
    override fun onSuccess(result: LocationEngineResult) {
        result.lastLocation?.run {
           // Log.e("location","lat= $latitude,lon =$longitude")
                mapBoxMap?.clear()
                centerLatLon = LatLng(latitude, longitude)
                cerMarker = mapBoxMap?.addMarker(MarkerOptions()
                        .setIcon(IconFactory.recreate(Constants.LOCATION, BitmapFactory.decodeResource(resources, R.drawable.ic_loc_marker)))
                        .setSnippet("")
                        .setTitle("")
                        .setPosition(centerLatLon!!))

                val position = CameraPosition.Builder()
                    .target(centerLatLon)
                    .zoom(17.0)
                    .tilt(20.0)
                    .build()
                mapBoxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000)
            //显示地名
          val geoCoder = Geocoder(this@GuardSettingActivity, Locale.getDefault())
          try {
              val addressList = geoCoder.getFromLocation(latitude, longitude, 1)
              val sb = StringBuilder()
              if (addressList.isNotEmpty()){
                  val address =  addressList[0]
                  for (i in 0..address.maxAddressLineIndex){
                      sb.append(address.getAddressLine(i))
                  }
                  tv_address.text = sb.toString()
               //   Log.e("location","address =${sb}")
              }
          }catch (e:Exception){
              e.printStackTrace()
          }


        }
    }

    override fun onFailure(exception: Exception) {
        exception.printStackTrace()
        showToast("定位失败")
    }
}