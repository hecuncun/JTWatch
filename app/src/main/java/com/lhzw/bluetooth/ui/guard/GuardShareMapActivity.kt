package com.lhzw.bluetooth.ui.guard
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.base.BaseMapActivity
import com.lhzw.bluetooth.bean.GuardLocationBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.DateUtils
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.widget.map.TrackLineOptions
import com.lhzw.bluetooth.wxapi.ShareUtils
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_guard_setting.*
import kotlinx.android.synthetic.main.activity_guard_share_map.*
import kotlinx.android.synthetic.main.activity_guard_share_map.iv_back
import kotlinx.android.synthetic.main.activity_guard_share_map.mapView
import kotlinx.android.synthetic.main.activity_guard_share_map.tv_address
import kotlinx.android.synthetic.main.activity_guard_share_map.tv_time
import org.litepal.LitePal
import java.lang.StringBuilder
import java.util.*

/**
 * Created by heCunCun on 2021/2/20
 */
class GuardShareMapActivity :BaseMapActivity(){
    private var gid: String by Preference(Constants.GUARD_ID, "0")//守护结束id
    private var mapBoxMap: MapboxMap? = null
    private var markerList = mutableListOf<MarkerOptions>()//定位点集合
    private var latLngList = mutableListOf<LatLng>()
    private var trackLineOptions: TrackLineOptions? = null
    override fun getMapView(): MapView =mapView
    override fun attachLayoutRes(): Int = R.layout.activity_guard_share_map

    override fun initData() {
        tv_time.text = DateUtils.dateToString(Date(),"yyyy-MM-dd HH:mm:ss")
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
            val marker = MarkerOptions().position(latLng).title("").snippet("")
            //.icon(IconFactory.getInstance(this).fromResource(R.drawable.ic_loc_marker))
            latLngList.add(latLng)
            markerList.add(marker)
        }
        mapBoxMap?.addMarkers(markerList)//画标记
        trackLineOptions?.drawTrackLine(mapView, latLngList.toTypedArray())//划线
        val latLng = LatLng(guardLocList.last().lat, guardLocList.last().lon)
        val position = CameraPosition.Builder()
                .target(latLng)
                .zoom(17.0)
                .tilt(20.0)
                .build()
        mapBoxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000)

        //显示地名
        val geoCoder = Geocoder(this@GuardShareMapActivity, Locale.getDefault())
        try {
            val addressList = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
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
    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }

        iv_wx.setOnClickListener { //分享微信
            mapBoxMap?.snapshot {
                if (BaseUtils.isAppInstall(this@GuardShareMapActivity, "com.tencent.mm")) {
                    ShareUtils.shareImageToWx(App.context,it, 0)
                }else{
                    Toast.makeText(this, "微信未安装", Toast.LENGTH_SHORT).show()
                }
            }
        }

        iv_wx_cycle.setOnClickListener {
            mapBoxMap?.snapshot {
                if (BaseUtils.isAppInstall(this@GuardShareMapActivity, "com.tencent.mm")) {
                     ShareUtils.shareImageToWx(App.context,it, 1)
                }else{
                    Toast.makeText(this, "微信未安装", Toast.LENGTH_SHORT).show()
                }
            }
        }

        iv_qq.setOnClickListener {
            mapBoxMap?.snapshot {
                if (BaseUtils.isAppInstall(this@GuardShareMapActivity, "com.tencent.mobileqq")) {
                    val send = Intent()
                    send.action = Intent.ACTION_SEND
                    send.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, it, null,null)))
                    send.type = "image/*";
                    send.setClassName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity")//qq
                    startActivityForResult(send, 0x11)
                }else{
                    Toast.makeText(this, "QQ未安装", Toast.LENGTH_SHORT).show()
                }
            }


        }
    }
}