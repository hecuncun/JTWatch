package com.lhzw.bluetooth.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.FlatSportBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.constants.ShareBgBitmap
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.glide.BlurBitmapUtil
import com.lhzw.bluetooth.glide.GlideUtils
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.wxapi.ShareUtils
import kotlinx.android.synthetic.main.activity_map_share_poster.*
import kotlinx.android.synthetic.main.activity_share_poster.im_bg_share
import kotlinx.android.synthetic.main.activity_share_poster.im_cancel
import kotlinx.android.synthetic.main.activity_share_poster.im_circle
import kotlinx.android.synthetic.main.activity_share_poster.im_qq
import kotlinx.android.synthetic.main.activity_share_poster.im_weixin
import kotlinx.android.synthetic.main.activity_share_poster.iv_head_photo
import kotlinx.android.synthetic.main.activity_share_poster.rl_share_poster
import kotlinx.android.synthetic.main.activity_share_poster.tv_save_poster
import java.io.File
import java.text.SimpleDateFormat


/**
 *
@author：created by xtqb
@description:
@date : 2020/5/6 10:50
 *
 */
class ShareMapPosterActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private var photoPath: String? by Preference(Constants.PHOTO_PATH, "")
    private val WX_QUEST = 0x0001
    private val QQ_QUEST = 0x0005
    private var path: String? = "/sdcard/share/xxxxxx.jpg"
    private var shareFile: File? = null
    private var nickName: String by Preference(Constants.NICK_NAME, "")

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//使activity都竖屏
        setContentView(R.layout.activity_map_share_poster)
        initView()
        setListener()
        initData()
        initPhotoError()
    }

    private fun saveShareUI2Bitmap() {
        if (shareFile == null) {
            rl_share_poster.isDrawingCacheEnabled = true
            rl_share_poster.buildDrawingCache()
            val bitmap = rl_share_poster.getDrawingCache()
            shareFile = BaseUtils.saveBitmapFile(bitmap, path)!!
            rl_share_poster.destroyDrawingCache()
        }
    }

    private fun initPhotoError() {
        // android 7.0系统解决拍照的问题
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
    }


    private fun initView() {
        tv_save_poster.paint.flags = Paint.UNDERLINE_TEXT_FLAG //下划线
        tv_save_poster.paint.isAntiAlias = true//抗锯齿
    }

    private fun setListener() {
        tv_save_poster.setOnClickListener(this)
        im_weixin.setOnClickListener(this)
        im_circle.setOnClickListener(this)
        im_qq.setOnClickListener(this)
        im_cancel.setOnClickListener(this)
        tv_save_poster.setOnTouchListener(this)
    }

    private fun initData() {
        GlideUtils.showCircleWithBorder(iv_head_photo, photoPath, R.drawable.pic_head, resources.getColor(R.color.white))
        var lenght = ShareBgBitmap.bg_bitmap?.width
        if (lenght!! > ShareBgBitmap.bg_bitmap?.height!!) {
            lenght = ShareBgBitmap.bg_bitmap!!.height
        }
        val bg = BlurBitmapUtil.centerSquareScaleBitmap(ShareBgBitmap.bg_bitmap!!, lenght)
        im_bg_share.alpha = 0.5f
        im_bg_share.setImageBitmap(bg)

        val mark: String = intent.getStringExtra("mark")
        val type: Int = intent.getIntExtra("type", 0)

        // 用户名
        tv_name.text = nickName

        when (type) {
            Constants.ACTIVITY_CLIMBING -> {

            }
            Constants.ACTIVITY_RUNNING -> {
                iv_sport_icon.setBackgroundResource(R.mipmap.sport_running)
                initSportData(mark)
            }
            Constants.ACTIVITY_HIKING -> {
                iv_sport_icon.setBackgroundResource(R.mipmap.sport_waking)
                initSportData(mark)
            }
            Constants.ACTIVITY_INDOOR -> {
                iv_sport_icon.setBackgroundResource(R.mipmap.sport_indoor)
                initSportData(mark)
            }
            Constants.ACTIVITY_REDING -> {
                iv_sport_icon.setBackgroundResource(R.mipmap.sport_waking)
                initSportData(mark)
            }

        }

    }

    private fun initSportData(mark: String) {
        val detail = CommOperation.query(FlatSportBean::class.java, "sport_detail_mark", mark)
        detail?.let {
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
            tv_allocation_speed.text = best_all_speed

            if (it[0].distance < 100) {
                findViewById<TextView>(R.id.tv_distance).text = "${String.format("%.2f", it[0].distance.toFloat() / 1000)}"
            } else {
                findViewById<TextView>(R.id.tv_distance).text = "${String.format("%.1f", it[0].distance.toFloat() / 1000)}"
            }
            tv_calorie.text = it[0].calorie.toString()
        }
    }

    override fun onClick(v: View?) {
        saveShareUI2Bitmap()
        when (v?.id) {
            R.id.tv_save_poster -> {
                rl_share_poster.isDrawingCacheEnabled = true
                rl_share_poster.buildDrawingCache()
                val bmp = rl_share_poster.getDrawingCache()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                if (BaseUtils.savePicture(bmp, "${formatter.format(System.currentTimeMillis())}.jpg")) {
                    Toast.makeText(this, "保存成功!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "保存失败!", Toast.LENGTH_SHORT).show()
                }

                rl_share_poster.destroyDrawingCache()
                finish()
            }
            R.id.im_weixin -> {
                if (!BaseUtils.isAppInstall(this@ShareMapPosterActivity, "com.tencent.mm")) {
                    Toast.makeText(this, "微信未安装", Toast.LENGTH_SHORT).show()
                    return
                }
//                val send = Intent()
//                send.action = Intent.ACTION_SEND
//                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    FileProvider.getUriForFile(this.applicationContext,
//                            "com.lhzw.bluetooth.fileprovider", shareFile!!.absoluteFile) //这个是版本大于Android7.0（包含）临时访问文件，没有这个会报异常
//                } else {
//                    Uri.fromFile(shareFile)
//                }
//                send.putExtra(Intent.EXTRA_STREAM, uri)
//                send.type = "image/*";
//                send.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");//微信朋友圈，仅支持分享图片
//                startActivityForResult(send, WX_QUEST)
                ShareUtils.shareImageToWx(App.context, shareFile!!.absolutePath, 0)
            }
            R.id.im_circle -> {
                if (!BaseUtils.isAppInstall(this@ShareMapPosterActivity, "com.tencent.mm")) {
                    Toast.makeText(this, "微信未安装", Toast.LENGTH_SHORT).show()
                    return
                }
//                val send = Intent()
//                send.action = Intent.ACTION_SEND
//                send.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile))
//                send.type = "image/*";
//                send.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");//微信朋友圈，仅支持分享图片
//                startActivityForResult(send, WX_QUEST);
                ShareUtils.shareImageToWx(App.context, shareFile!!.absolutePath, 1)
                finish()
            }
            R.id.im_qq -> {
                if (!BaseUtils.isAppInstall(this@ShareMapPosterActivity, "com.tencent.mobileqq")) {
                    Toast.makeText(this, "QQ未安装", Toast.LENGTH_SHORT).show()
                    return
                }
                val send = Intent()
                send.action = Intent.ACTION_SEND
                send.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile))
                send.type = "image/*";
                send.setClassName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");//微信朋友圈，仅支持分享图片
                startActivityForResult(send, WX_QUEST);
            }
            R.id.im_cancel -> {
                finish()
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            R.id.tv_save_poster -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        tv_save_poster.setTextColor(getColor(R.color.white))
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        tv_save_poster.setTextColor(getColor(R.color.gray_little1))
                    }
                }
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        shareFile?.let {
            if (it.isFile && it.exists()) {
                it.delete()
            }
        }
        shareFile = null
        path = null
    }

}