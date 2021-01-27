package com.lhzw.bluetooth.mvp.presenter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.ClimbingSportBean
import com.lhzw.bluetooth.bean.FlatSportBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.glide.GlideUtils
import com.lhzw.bluetooth.mvp.contract.SportConstract
import com.lhzw.bluetooth.mvp.model.SportModel
import com.lhzw.bluetooth.ui.activity.ShareMapPosterActivity
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.LocationUtils
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.uitls.douglas.Douglas
import com.lhzw.bluetooth.view.ShareShareDialog
import com.lhzw.bluetooth.view.SportTrailView
import com.lhzw.kotlinmvp.presenter.BaseIPresenter
import kotlinx.android.synthetic.main.activity_sport_info.*
import java.security.MessageDigest
import java.util.*


/**
 *
@author：created by xtqb
@description: 实现处理model数据 刷新View界面
@date : 2019/11/12 10:28
 *
 */

class MainSportPresenter(var mark: String, var duration: String, val type: Int) : BaseIPresenter<SportConstract.View>(), SportConstract.Presenter, LocationUtils.ILocationCallBack, LocationSource, AMap.CancelableCallback {
    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var aMap: AMap? = null
    private var locationUtils: LocationUtils? = null
    private val DRAWPATH = 0x0001
    private var isMapAnimOver = false
    private val ANIMATION = 0x0005
    private val SHOTSCREEN = 0x0010
    private var photoPath: String? by Preference(Constants.PHOTO_PATH, "")
    private var nickName: String  by Preference(Constants.NICK_NAME, "")
    private var currentMarker: Marker? = null
    private var isOver = false
    override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener?) {
        mListener = onLocationChangedListener;
//        locationUtils?.startLocate()
    }

    override fun deactivate() {
        mListener = null
    }

    override fun callBack(str: String, lat: Double, lgt: Double, aMapLocation: AMapLocation) {
        //根据获取的经纬度，将地图移动到定位位置
        aMap?.apply {
//            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lgt), Constants.ZOOM))

//            //添加定位图标
//            addMarker(locationUtils?.getMarkerOption(str, lat, lgt));
//            Log.e("LatLon", "lat  $lat   lgt   $lgt")

        }
//        mListener?.onLocationChanged(aMapLocation)
    }

    override fun requirePermission(activity: Activity): Boolean {
        try {
            var needPermissions = arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Constants.BACK_LOCATION_PERMISSION
            )
            if (Build.VERSION.SDK_INT >= 23) {
                return model.checkPermissions(activity, needPermissions)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

    //初始化地图
    override fun initMap(activity: Activity, mMapView: MapView?): AMap? {
        aMap = model.initMap(mMapView)
        // 定位蓝点
        aMap?.apply {
            mapType = AMap.MAP_TYPE_SATELLITE// 矢量地图模式
            uiSettings.isZoomControlsEnabled = false//隐藏放大缩小按钮
//            uiSettings.isScrollGesturesEnabled = false
//            uiSettings.isZoomGesturesEnabled = false
            uiSettings.isRotateGesturesEnabled = false
//            uiSettings.isTiltGesturesEnabled = false
//            uiSettings.setAllGesturesEnabled(false)
//            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(Constants.LAT, Constants.LGT), Constants.ZOOM))
            locationUtils = LocationUtils()
            locationUtils?.setLocationCallBack(this@MainSportPresenter)
            setOnMarkerClickListener(markerListner)
            //设置定位监听
            setLocationSource(this@MainSportPresenter);
            //设置缩放级别
//            moveCamera(CameraUpdateFactory.zoomTo(Constants.ZOOM))
            //显示定位层并可触发，默认false
            isMyLocationEnabled = true
            Log.e("Tag", "drawPaths ....")
            // 绘制路径
            val msg = Message()
            msg.what = DRAWPATH
            msg.obj = activity
            mHandler.sendMessageDelayed(msg, 150)
        }
        return aMap!!
    }

    override fun getSha1(): String? {
        try {
            val info = App.context.packageManager
                    .getPackageInfo(App.context.packageName,
                            PackageManager.GET_SIGNATURES)
            val cert = info.signatures[0].toByteArray()
            val md = MessageDigest.getInstance("SHA1")
            val publicKey = md.digest(cert)
            val hexString = StringBuffer()
            for (i in publicKey.indices) {
                val appendString = Integer.toHexString(0xFF and publicKey[i].toInt())
                        .toUpperCase(Locale.US)
                if (appendString.length == 1)
                    hexString.append("0")
                hexString.append(appendString)
            }
            return hexString.toString()
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return null
    }


    // 初始化图表
    override fun initChart(activity: Activity, convertView: View) {
        model.initChart(activity, convertView)
    }

    // 显示分享对话框
    override fun showSharePopuWindow(activity: Activity, stareBitmap: Bitmap?) {
        var dialog = ShareShareDialog(activity, stareBitmap)
        dialog.window.decorView.setBackgroundResource(R.color.transparent)
        dialog.showDialog()


        val windowManager = activity.windowManager as WindowManager
        val display = windowManager.defaultDisplay
        val lp = dialog.window.attributes as WindowManager.LayoutParams
        lp.width = (display.getWidth()) - BaseUtils.dip2px(12)//设置宽度
        dialog.window.attributes = lp;
    }

    // 跳转分享acitivity
    override fun startShareActivity(activity: Activity?, path: String?) {
        activity?.let {
            val intent = Intent(it, ShareMapPosterActivity::class.java)
            intent.putExtra("mark", mark)
            intent.putExtra("type", type)
            it.startActivity(intent)
        }
    }


    override fun initView(activity: Activity, convertView: View) {
        // 修改字体
        model.initFont(activity, convertView)
        // 更换头像
        GlideUtils.showCircleWithBorder(convertView.findViewById<ImageView>(R.id.iv_head_photo),
                photoPath, R.drawable.pic_head, activity.resources.getColor(R.color.white))
        // 用户名
        convertView.findViewById<TextView>(R.id.tv_per_name).text = nickName
        // 步数
        val list = model.queryData(mark = mark, type = Constants.STEP)
        var step = 0
        var step_max = 0
        list?.forEach {
            step += it.value;
            if (step_max < it.value) {
                step_max = it.value
            }
        }
        if (step <= 0) {
            convertView.findViewById<TextView>(R.id.tv_stride_frequency).text = "$step"
            convertView.findViewById<TextView>(R.id.tv_stride_frequency_top).text = "$step"
            convertView.findViewById<TextView>(R.id.tv_step_stride_av).text = "$step"
        } else {
            convertView.findViewById<TextView>(R.id.tv_stride_frequency).text = "${step / list?.size!!}"
            convertView.findViewById<TextView>(R.id.tv_stride_frequency_top).text = "${step / list?.size!!}"
            convertView.findViewById<TextView>(R.id.tv_step_stride_av).text = "${step / list?.size!!}"
        }
        convertView.findViewById<TextView>(R.id.tv_steps_num).text = "$step"
        convertView.findViewById<TextView>(R.id.tv_steps_num_top).text = "$step"
        // 图表数据适配
        convertView.findViewById<TextView>(R.id.tv_step_stride_best).text = "$step_max"

        convertView.findViewById<TextView>(R.id.tv_duration).text = duration
        convertView.findViewById<TextView>(R.id.tv_duration_top).text = duration
        if (type == Constants.ACTIVITY_CLIMBING) {
            val detail = model.queryData<ClimbingSportBean>(mark)
            // 暂时没有
        } else {
            val detail = model.queryData<FlatSportBean>(mark)
            detail?.let {
//                convertView.findViewById<TextView>(R.id.tv_step_num).text = "${it[0].step_num}"
//                activity.tv_distance.text = "${it[0].distance}"
                convertView.findViewById<TextView>(R.id.tv_heart_rate).text = "${it[0].average_heart_rate}"
                convertView.findViewById<TextView>(R.id.tv_heart_rate_top).text = "${it[0].average_heart_rate}"
                convertView.findViewById<TextView>(R.id.tv_speed_heart_av).text = "${it[0].average_heart_rate}"
                convertView.findViewById<TextView>(R.id.tv_speed_heart_best).text = "${it[0].max_heart_rate}"
                if (it[0].distance < 100) {
                    activity.tv_distance.text = "${String.format("%.2f", it[0].distance.toFloat() / 1000)}"
                } else {
                    activity.tv_distance.text = "${String.format("%.1f", it[0].distance.toFloat() / 1000)}"
                }

                val speed_allocation_av = BaseUtils.intToByteArray(it[0].speed)
                if (speed_allocation_av[0] < 0) speed_allocation_av[0] = 0
                var av_all_speed = ""
                if (speed_allocation_av[0] < 0x0A) {
                    av_all_speed += "0"
                }
                av_all_speed += "${speed_allocation_av[0].toInt() and 0xFF}${"\'"}"
                if (speed_allocation_av[1] < 0) speed_allocation_av[1] = 0
                if (speed_allocation_av[1] < 0x0A) {
                    av_all_speed += "0"
                }
                av_all_speed += "${speed_allocation_av[1].toInt() and 0xFF}${"\""}"
                convertView.findViewById<TextView>(R.id.tv_allocation_speed).text = av_all_speed
                convertView.findViewById<TextView>(R.id.tv_allocation_speed_top).text = av_all_speed
                val speed_allocation_best = BaseUtils.intToByteArray(it[0].best_speed)
                var best_all_speed = ""
                if (speed_allocation_best[0] < 0) speed_allocation_best[0] = 0
                if (speed_allocation_best[0] < 0x0A) {
                    best_all_speed += "0"
                }
                best_all_speed += "${speed_allocation_best[0].toInt() and 0xFF}${"\'"}"
                if (speed_allocation_best[1] < 0) speed_allocation_best[1] = 0
                if (speed_allocation_best[1] < 0x0A) {
                    best_all_speed += "0"
                }
                best_all_speed += "${speed_allocation_best[1].toInt() and 0xFF}${"\""}"
                // 最佳配速
                convertView.findViewById<TextView>(R.id.tv_best_allocation_speed).text = best_all_speed
                convertView.findViewById<TextView>(R.id.tv_best_allocation_speed_top).text = best_all_speed
                // 热量
                if (it[0].calorie < 100) {
                    activity.tv_calorie.text = "${String.format("%.2f", it[0].calorie.toFloat() / 1000)}"
                } else {
                    activity.tv_calorie.text = "${String.format("%.0f", it[0].calorie.toFloat() / 1000)}"
                }
            }
        }
    }

    override fun getCurrentMarker(): Marker? = currentMarker

    private var markerListner: AMap.OnMarkerClickListener? = AMap.OnMarkerClickListener {
        Log.e("onMap", "onClick ....  12")
        locationUtils?.jumpPoint(it!!, aMap!!.projection)
        it.showInfoWindow()
        currentMarker = it
        true
    }

    fun drawPaths(activity: Activity) {

        // 绘制轨迹
        /*
        var minLat = 90.0
        var maxlgt = 0.0
        var maxLat = 0.0
        var minLgt = 180.0
        var latLngs = model.queryData(mark, Constants.GPS)
        BaseUtils.ifNotNull(latLngs, aMap) { it, amp ->
            var list = ArrayList<LatLng>()
            it.forEach {
                var tmp = LatLng(it.gps_latitude, it.gps_longitude)
                if (it.gps_latitude < minLat) {
                    minLat = it.gps_latitude
                }
                if (it.gps_longitude > maxlgt) {
                    maxlgt = it.gps_longitude
                }
                if (it.gps_latitude > maxLat) {
                    maxLat = it.gps_latitude
                }
                if (it.gps_longitude < minLgt) {
                    minLgt = it.gps_longitude
                }
                list.add(tmp)
            }
            Log.e("LatLon", "draw paths ....")
            if (list.size > 0) {
                var northeast = LatLng(minLat, maxlgt)
                var southwest = LatLng(maxLat, minLgt)
                var bounds = LatLngBounds.Builder().include(northeast)
                        .include(southwest).build();
                var cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
//                aMap?.animateCamera(cameraUpdate, 100L, null);
                aMap?.moveCamera(cameraUpdate)
                locationUtils?.drawPath(amp, list)
//                aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(list[0], Constants.ZOOM))

    }
    }
     */
        val b = LatLngBounds.builder()
        var latLngs = model.queryData(mark, Constants.GPS)
        if (latLngs == null || latLngs.isEmpty()) {
            isOver = true
            return
        }
        BaseUtils.ifNotNull(latLngs, aMap) { it, amp ->
            var list = ArrayList<LatLng>()
            it.forEach {
                if (BaseUtils.outOfChina(it.gps_latitude, it.gps_longitude)) return@forEach
                var tmp = LatLng(it.gps_latitude, it.gps_longitude)
                b.include(tmp)
                list.add(tmp)
            }
            setMapAnimOver(false)
            val bounds: LatLngBounds = b.build()
            val top_padding: Int = BaseUtils.dip2px(50)
            val bottom_padding: Int = BaseUtils.dip2px(40 + 30 + 160)
            val left_right_padding: Int = BaseUtils.dip2px(50)
            var douglasList = Douglas(list, 3.0).compress()
            amp.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(bounds, left_right_padding, left_right_padding, top_padding, bottom_padding), 900L, object : AMap.CancelableCallback {
                override fun onFinish() {
                    Log.e("onMap", "draw path success ....")
                    setMapAnimOver(true)
//                    mHandler.sendEmptyMessageDelayed(ANIMATION, 150)
//                    activity?.trailview.setOnClickListener {
//                        mView?.toastMsg("等待动画结束")
//                    }
                    val screenPoints = ArrayList<Point>()
                    douglasList?.forEach {
                        screenPoints.add(amp.projection.toScreenLocation(it))
                    }
                    activity?.runOnUiThread {
                        activity?.trailview?.drawSportLine(screenPoints, R.mipmap.location_start, R.drawable.icon_ball, object : SportTrailView.OnTrailChangeListener {
                            override fun onFinish() {
                                activity?.trailview?.visibility = View.GONE
                                Thread {
                                    model?.getDistanceMap()?.forEach { (t, u) ->
                                        Log.e("DistanceMap", "t  " + t + "  u  " + u)
                                    }
                                    locationUtils?.drawPath(amp, list, model?.getDistanceMap())
                                    val msg = Message()
                                    msg.what = SHOTSCREEN
                                    msg.obj = activity
                                    mHandler.sendMessageDelayed(msg, 200)
                                }.start()
                            }
                        })
                    }
                }

                override fun onCancel() {
                    Log.e("onMap", "draw path fail ....")
                    activity?.trailview.visibility = View.GONE
                }
            })
//            amp.moveCamera(CameraUpdateFactory.newLatLngBoundsRect(bounds,left_right_padding, left_right_padding, top_padding, bottom_padding))
//            locationUtils?.drawPath(amp, list, model?.getDistanceMap())
        }
    }

    override fun detachView() {
        super.detachView()
        locationUtils?.detachCallBack()
        locationUtils = null
        currentMarker = null
        aMap = null
    }

    private var model: SportModel = SportModel(mark)
    override fun onFinish() {
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(Constants.ZOOM))
    }

    override fun onCancel() {

    }

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                DRAWPATH -> {
                    val activity = msg.obj as Activity
                    Thread { drawPaths(activity) }.start()
                }
                ANIMATION -> {

                }
                SHOTSCREEN -> {
                    var listener = msg.obj as AMap.OnMapScreenShotListener
                    aMap?.getMapScreenShot(listener)
                    setAnimationState(true)
                    mView?.cancel()
                }
            }
        }
    }

    fun setAnimationState(state: Boolean) {
        isOver = state
    }

    fun getAnimationState(): Boolean {
        return isOver
    }

    fun isMapAnimOver() = isMapAnimOver
    fun setMapAnimOver(state: Boolean) {
        isMapAnimOver = state
    }
}