package com.lhzw.bluetooth.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ScrollView
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.mvp.contract.SportConstract
import com.lhzw.bluetooth.mvp.presenter.MainSportPresenter
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.BitmapUtil
import com.lhzw.kotlinmvp.base.BaseSportActivity
import com.umeng.socialize.UMShareAPI
import com.xw.repo.supl.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_sport_info.*
import kotlinx.android.synthetic.main.panel_content_view.*
import kotlinx.android.synthetic.main.toolbar.*
import me.jessyan.autosize.internal.CancelAdapt
import java.io.File


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/18 15:25
 *
 */

@Suppress("DEPRECATION")
class SportInfoActivity : BaseSportActivity<MainSportPresenter>(), SportConstract.View, AMap.OnMapClickListener, AMap.OnMapScreenShotListener, CancelAdapt {
    private var aMap: AMap? = null
    private var isPanelVisible = false
    private val PICK_PHOTO = 0x00102
    override fun getLayoutId(): Int {
        return R.layout.activity_sport_info
    }

    override fun initView() {
//        CommonUtil.setMaxAspect(this)
        initTileBar()
        // 设置高度
//        val params = rl_map.layoutParams
//        var bar = findViewById<LinearLayout>(com.lhzw.bluetooth.R.id.toolbar)
//        //  实际高度   =         屏幕高度                       -        标题栏高度  -     状态栏高度                               -  虚拟键盘高度
//        params.height = resources.displayMetrics.heightPixels - bar.measuredHeight - CommonUtil.getStatusBarHeight(this) - CommonUtil.getNavigationBarHeight(this)
//        rl_map.layoutParams = params
        // 界面数据适配
        val mark = intent.getStringExtra("mark")
        val type = intent.getIntExtra("type", 0)
        val duration = intent.getStringExtra("duration")
        Log.e("type", "$type       -----------------")
        // 活动图标
        when (type) {
            //跑步
            Constants.ACTIVITY_RUNNING -> {
                iv_sport_icon.setBackgroundResource(R.mipmap.sport_running)
            }
            //骑车
            Constants.ACTIVITY_REDING -> {
                iv_sport_icon.setBackgroundResource(R.mipmap.sport_waking)
            }
            //徒步
            Constants.ACTIVITY_HIKING -> {
                iv_sport_icon.setBackgroundResource(R.mipmap.sport_waking)
            }
            //登山
            Constants.ACTIVITY_CLIMBING -> {
                iv_sport_icon.setBackgroundResource(R.mipmap.sport_waking)
            }
            //室内跑
            Constants.ACTIVITY_INDOOR -> {
                map.visibility = View.GONE
                im_bg_share.visibility = View.VISIBLE
                iv_sport_icon.setBackgroundResource(R.mipmap.sport_indoor)
                if (!TextUtils.isEmpty(indoor_backgroud)) {
                    try {
                        scBitmapMap = BitmapFactory.decodeFile(indoor_backgroud)
                        var with = scBitmapMap?.width!!
                        if (scBitmapMap?.width!! > scBitmapMap?.height!!) {
                            with = scBitmapMap?.height!!
                        }
                        if (with > 100) {
                            with = 100
                        }
                        scBitmapShot = Bitmap.createBitmap(scBitmapMap, 0, 100, with, with)
                        BaseUtils.savePicture(scBitmapShot, "sport_gaode_map_shot.jpg")
                        displayImage(indoor_backgroud)
                    } catch (e: java.lang.Exception) {
                        indoor_backgroud = ""
                        scBitmapMap = BitmapFactory.decodeResource(resources, R.drawable.bg_share_06)
                        scBitmapShot = Bitmap.createBitmap(scBitmapMap, 0, 100, scBitmapMap?.width!!, scBitmapMap?.width!!)
                    }
                } else {
                    scBitmapMap = BitmapFactory.decodeResource(resources, R.drawable.bg_share_06)
                    scBitmapShot = Bitmap.createBitmap(scBitmapMap, 0, 100, scBitmapMap?.width!!, scBitmapMap?.width!!)
                }

                isIndoor = true
            }
        }
        mPresenter = MainSportPresenter(mark, "${duration}", type)
        Log.e("Tag", "mPresenter == null ? ${mPresenter == null}")
        mPresenter?.apply {
            attachView(this@SportInfoActivity)
            initChart(this@SportInfoActivity, convertView!!)
            initView(this@SportInfoActivity, convertView!!)
            if (!isIndoor && requirePermission(this@SportInfoActivity)) {
                aMap = initMap(this@SportInfoActivity, mMapView)
                aMap?.setOnMapClickListener(this@SportInfoActivity)
            }
        }
        v_cover.setOnClickListener {
            if (!isIndoor) {
                if (mPresenter == null || !getAnimationState()) {
                    return@setOnClickListener
                }
            }
            it.visibility = View.GONE
        }
        if (isIndoor) {
            im_bg_share.setOnClickListener {
                body(it)
            }
            im_replace.setOnClickListener {
                repalce()
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        mPresenter?.let {
            if (!it.isMapAnimOver()) {
                Thread { it.drawPaths(this@SportInfoActivity) }.start()
            }
        }
    }

    private fun initTileBar() {
        toolbar_title.text = intent.getStringExtra("ymt")
        im_back.visibility = View.VISIBLE
        im_back.setOnClickListener {
            if (!isIndoor && !getAnimationState()) {
                return@setOnClickListener
            }
            finish()
        }
        toolbar_right_img.visibility = View.VISIBLE
        toolbar_right_img.setBackgroundResource(R.drawable.icon_share_def)
        toolbar_right_img.setOnClickListener {

            if (!isIndoor && !getAnimationState()) {
                return@setOnClickListener
            }
            if (!isIndoor && scBitmapMap == null) {
                showToast("没有轨迹")
                return@setOnClickListener
            }

            sliding_up_panel_layout.hiddedPanel()

            title_toolbar.isDrawingCacheEnabled = true
            title_toolbar.buildDrawingCache()
            val toolbarBitmap = title_toolbar.drawingCache

            iv_sport_icon.isDrawingCacheEnabled = true
            iv_sport_icon.buildDrawingCache()
            val typeBitmap = iv_sport_icon.drawingCache

            rl_distance.isDrawingCacheEnabled = true
            rl_distance.buildDrawingCache()
            val distanceBitmap = rl_distance.drawingCache

            rl_calorie.isDrawingCacheEnabled = true
            rl_calorie.buildDrawingCache()
            val calorieBitmap = rl_calorie.drawingCache

            panel_collapse_layout.isDrawingCacheEnabled = true
            panel_collapse_layout.buildDrawingCache()
            val bitmap = panel_collapse_layout.drawingCache

            panelViewList[0].getConvertView()!!.findViewById<ScrollView>(R.id.scrollview).isVerticalScrollBarEnabled = false
            val dataBitmap = BitmapUtil.shotScrollView(panelViewList[0].getConvertView()!!.findViewById(R.id.scrollview))

            val space_map = BaseUtils.dip2px(10)
            val shareBitmap = Bitmap.createBitmap(dataBitmap.width, dataBitmap.height + window.windowManager.defaultDisplay.height + space_map, Bitmap.Config.ARGB_8888)
            shareBitmap.setHasAlpha(true)
            val cv = Canvas(shareBitmap)
            val pain = Paint()
            pain.isAntiAlias = true
            pain.color = getColor(R.color.gray_little1)
            cv.drawRect(Rect(0, 0, dataBitmap.width, dataBitmap.height + window.windowManager.defaultDisplay.height + space_map), pain)
            if (scBitmapMap == null) {
                showToast("没有轨迹")
                return@setOnClickListener
            }
            cv.drawBitmap(scBitmapMap, Rect(0, 0, scBitmapMap!!.width, scBitmapMap!!.height), Rect(0, 0, scBitmapMap!!.width, scBitmapMap!!.height), null)
            cv.drawBitmap(toolbarBitmap, Rect(0, 0, toolbarBitmap!!.width, toolbarBitmap!!.height), Rect(0, 0, toolbarBitmap!!.width, toolbarBitmap!!.height), null)
            val im_margin_h = BaseUtils.dip2px(30)
            val im_margin_v = BaseUtils.dip2px(60)
            cv.drawBitmap(typeBitmap, Rect(0, 0, typeBitmap!!.width, typeBitmap!!.height), Rect(0 + im_margin_h, 0 + im_margin_v, typeBitmap!!.width + im_margin_h, typeBitmap!!.height + im_margin_v), null)

            val rl_margin_h = BaseUtils.dip2px(25)
            val rl_margin_v = BaseUtils.dip2px(50)
            val space = BaseUtils.dip2px(5)
            val sc_with = window.windowManager.defaultDisplay.width
            val sc_height = window.windowManager.defaultDisplay.height
            cv.drawBitmap(distanceBitmap, Rect(0, 0, distanceBitmap!!.width, distanceBitmap!!.height), Rect(sc_with - rl_margin_h - distanceBitmap!!.width, rl_margin_v, sc_with - rl_margin_h, distanceBitmap!!.height + rl_margin_v), null)
            cv.drawBitmap(calorieBitmap, Rect(0, 0, calorieBitmap!!.width, calorieBitmap!!.height), Rect(sc_with - rl_margin_h - distanceBitmap!!.width - calorieBitmap.width - space, rl_margin_v, sc_with - rl_margin_h - distanceBitmap!!.width - space, distanceBitmap!!.height + rl_margin_v), null)
            val padding = BaseUtils.dip2px(6)
            val padding1 = BaseUtils.dip2px(1)
            val panel_h = BaseUtils.dip2px(210)

            val cropBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap!!.width, panel_h, null, false)
            cv.drawBitmap(cropBitmap, Rect(0, 0, cropBitmap!!.width, cropBitmap!!.height), Rect(0 + padding, sc_height - panel_h, sc_with - padding, sc_height), null)
            cv.drawBitmap(dataBitmap, Rect(0, 0, dataBitmap!!.width, dataBitmap!!.height), Rect(padding1, sc_height + space_map, sc_with - padding1, sc_height + dataBitmap!!.height + space_map), null)

            title_toolbar.destroyDrawingCache()
            panel_collapse_layout.destroyDrawingCache()
            iv_sport_icon.destroyDrawingCache()
            rl_distance.destroyDrawingCache()
            rl_calorie.destroyDrawingCache()
            panelViewList[0].getConvertView()!!.findViewById<ScrollView>(R.id.scrollview).destroyDrawingCache()
            Log.e("Bitmap", "${shareBitmap == null}  ${shareBitmap.width}  ${shareBitmap.height}")
            mPresenter?.showSharePopuWindow(this, shareBitmap)
            bitmap.recycle()
            toolbarBitmap.recycle()
            typeBitmap.recycle()
            distanceBitmap.recycle()
            calorieBitmap.recycle()
            dataBitmap.recycle()
            cropBitmap.recycle()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("Tag", "requestCode  $requestCode")
        when (requestCode) {
            Constants.REQUESTCODE -> {
                if (Build.VERSION.SDK_INT >= 23) {
                    aMap = mPresenter?.initMap(this@SportInfoActivity, mMapView!!)
                    aMap?.setOnMapClickListener(this@SportInfoActivity)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_PHOTO -> {
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        var imagePath: String? = null
                        if (Build.VERSION.SDK_INT >= 19) {
                            // 4.4及以上系统使用这个方法处理图片
                            imagePath = handleImageOnKitKat(data);
                        } else {
                            // 4.4以下系统使用这个方法处理图片
                            imagePath = handleImageBeforeKitKat(data);
                        }
                        indoor_backgroud = imagePath
                        scBitmapMap = BitmapFactory.decodeFile(imagePath)
                        var with = scBitmapMap?.width!!
                        if (scBitmapMap?.width!! > scBitmapMap?.height!!) {
                            with = scBitmapMap?.height!!
                        }
                        if (with > 100) {
                            with = 100
                        }
                        scBitmapShot = Bitmap.createBitmap(scBitmapMap, 0, 100, with, with)
                        BaseUtils.savePicture(scBitmapShot, "sport_gaode_map_shot.jpg")
                        Log.e("ImagePath", "resultCode : $resultCode , $imagePath")
                        displayImage(imagePath)
                    }
                } catch (e: Exception) {
                    showToast("图片尺寸不匹配， ")
                }
            }
            else -> {
                UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun displayImage(path: String?) {
        if (path != null) {
            Log.e("ImagePath", "path : $path")
            val bg = BitmapFactory.decodeFile(path)
            im_bg_share.setImageBitmap(bg);
        } else {
            showToast("获取图片失败")
        }
    }

    // 刷新view
    override fun reflesh() {

    }


    override fun onDestroy() {
        super.onDestroy()
        aMap?.apply {
            aMap = null
        }
    }

    override fun onMapClick(latLgt: LatLng?) {
        Log.e("onMap", "onClick ....")
        mPresenter?.getCurrentMarker()?.let {
            if (it.isInfoWindowShown) {
                it.hideInfoWindow()
            } else {
                dealPanelState()
            }
        }
        if (mPresenter?.getCurrentMarker() == null) {
            dealPanelState()
        }
    }

    // 室内运动 点击时间
    private val body: (view: View) -> Unit = {
        if (mPresenter != null) {
            dealPanelState()
        }

        if (isPanelVisible) {
            im_replace.visibility = View.VISIBLE
        } else {
            im_replace.visibility = View.GONE
        }
    }

    // 更换背景图片
    private val repalce: () -> Unit = {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        //Intent.ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT"
        intent.type = "image/*"
        startActivityForResult(intent, PICK_PHOTO) // 打开相册
    }

    private fun dealPanelState() {
        if (panelViewList[0].slideState == SlidingUpPanelLayout.COLLAPSED) {
            isPanelVisible = true
            sliding_up_panel_layout.hiddedPanel()
        } else if (panelViewList[0].slideState == SlidingUpPanelLayout.HIDDEN) {
            isPanelVisible = false
            sliding_up_panel_layout.collapsePanel()
        }
    }

    /**
     * 地图截屏回调此方法,并返回截屏一瞬间地图是否渲染完成。
     */
    override fun onMapScreenShot(bitmap: Bitmap?) {

    }

    /**
     * bitmap - 截屏返回的bitmap对象。
     * status - 地图渲染状态， 1：地图渲染完成，0：未渲染完成
     */
    override fun onMapScreenShot(bitmap: Bitmap?, pState: Int) {
        Log.e("onMap", "scBitmapShot  ..............  $pState")
        if (pState == 1) {
            val file = File(scMapShotPath)
            if (file.isFile && file.exists()) {
                System.gc();
                file.delete()
            }
            // 保存原始地图数据

            scBitmapMap = bitmap
            //从屏幕整张图片中截取指定区域
            scBitmapShot = Bitmap.createBitmap(bitmap, 0, 100, bitmap?.width!!, bitmap?.width!!)
            BaseUtils.savePicture(scBitmapShot, "sport_gaode_map_shot.jpg")
        }
    }
}