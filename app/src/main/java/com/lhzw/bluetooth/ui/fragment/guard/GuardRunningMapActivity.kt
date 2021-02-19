package com.lhzw.bluetooth.ui.fragment.guard

import android.hardware.camera2.params.InputConfiguration
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseMapActivity
import com.lhzw.bluetooth.bean.GuardLocationBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.RefreshGuardState
import com.lhzw.bluetooth.event.RefreshMapLocEvent
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.widget.map.TrackLineOptions
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_guard_running_map.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal

/**
 * Created by heCunCun on 2021/2/6
 */
class GuardRunningMapActivity : BaseMapActivity() {
    private var guardEndTime: Long by Preference(Constants.GUARD_END_TIME, 0)//守护开启状态
    private var countDownTimer: CountDownTimer? = null
    private var gid: String by Preference(Constants.GUARD_ID, "0")//守护结束id
    private var mapBoxMap: MapboxMap? = null
    private var markerList = mutableListOf<MarkerOptions>()//定位点集合
    private var latLngList = mutableListOf<LatLng>()
    private var trackLineOptions: TrackLineOptions? = null
    override fun useEventBus(): Boolean = true

    override fun attachLayoutRes(): Int = R.layout.activity_guard_running_map

    override fun initData() {
        refreshGuardState()
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
            drawLocMarker()
        }


    }


    private fun drawLocMarker() {
        markerList.clear()
        mapBoxMap?.clear()
        latLngList.clear()
        trackLineOptions = null
        trackLineOptions = TrackLineOptions(mapView, mapBoxMap)
        //查询定位点
        val guardLocList = LitePal.where("gid=?", gid).find(GuardLocationBean::class.java)
        for (guardLocationBean in guardLocList) {
            val latLng = LatLng(guardLocationBean.lat, guardLocationBean.lon)
            val marker = MarkerOptions().position(latLng).title("").snippet("").icon(IconFactory.getInstance(this).fromResource(R.drawable.ic_loc_marker))
            latLngList.add(latLng)
            markerList.add(marker)
        }
        mapBoxMap?.addMarkers(markerList)//画标记
        trackLineOptions?.drawTrackLine(mapView, latLngList.toTypedArray())//划线
        val latLng = LatLng(guardLocList.last().lat, guardLocList.last().lon)
        Log.e("GuardRunningMapActivity", "lat =${latLng.latitude},Lng= ${latLng.longitude}")
        val position = CameraPosition.Builder()
                .target(latLng)
                .zoom(17.0)
                .tilt(20.0)
                .build()
        mapBoxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000)
    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
        iv_finish.setOnClickListener {
            //完成
            countDownTimer?.cancel()
            guardEndTime = 0
            refreshGuardState()
            iv_finish.visibility = View.GONE
            EventBus.getDefault().post(RefreshGuardState())
        }
    }

    private fun refreshGuardState() {
        if (System.currentTimeMillis() < guardEndTime) {
            //守护开启中
            countDownTimer = object : CountDownTimer(guardEndTime - System.currentTimeMillis(), 1000) {
                override fun onTick(p0: Long) {
                    tv_time.text = DateUtils.longTimeToHMS(p0)
                }

                override fun onFinish() {
                    tv_time.visibility = View.INVISIBLE
                    tv_state.text = "泰安全守护完成"
                    iv_finish.visibility = View.GONE
                }

            }
            countDownTimer?.start()
        } else {
            tv_time.visibility = View.INVISIBLE
            tv_state.text = "泰安全守护完成"
        }

    }

    override fun getMapView(): MapView = mapView


    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshLocMap(e: RefreshMapLocEvent) {
        Log.e("GuardRunningMapActivity", "刷新map页面")
        drawLocMarker()
    }
}