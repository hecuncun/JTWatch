package com.lhzw.bluetooth.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.uitls.BaseUtils
import com.tencent.bugly.Bugly.applicationContext
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.child_share.*
import java.io.File


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/26 16:42
 *
 */


class ShareShareDialog(private var mContext: Activity?, private var shareBitmap: Bitmap?) : BottomSheetDialog(mContext!!), View.OnClickListener {
    private val WX_QUEST = 0x0001
    private val QQ_QUEST = 0x0005
    private var path: String? = "/sdcard/share/xxxxxx.jpg"
    private var shareFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var convertview: View? = null

    init {
        convertview = LayoutInflater.from(mContext).inflate(R.layout.dialog_share_sheet, null)
        convertview?.apply {
            setContentView(convertview)
            val parent = parent as ViewGroup
            parent.setBackgroundResource(android.R.color.transparent)
            im_qq.setOnClickListener(this@ShareShareDialog)
            im_circle.setOnClickListener(this@ShareShareDialog)
            im_weixin.setOnClickListener(this@ShareShareDialog)
            im_cancel.setOnClickListener(this@ShareShareDialog)
        }
        setOnDismissListener { onDetroy() }
    }

    override fun onClick(v: View?) {
        var platform: SHARE_MEDIA? = null
        if (!BaseUtils.isNetworkConnected()) {
            Toast.makeText(mContext!!, mContext?.getString(R.string.net_connect_error), Toast.LENGTH_LONG).show()
            return
        }
        when (v?.id) {
            R.id.im_qq -> {
                if (!BaseUtils.isAppInstall(Constants.QQ)) {
                    Toast.makeText(mContext!!, mContext?.getString(R.string.app_installed_error_qq), Toast.LENGTH_LONG).show()
                    this.dismiss()
                    return
                }
                platform = SHARE_MEDIA.QQ
            }
            R.id.im_circle -> {
                if (!BaseUtils.isAppInstall(Constants.WEIXIN)) {
                    Toast.makeText(mContext!!, mContext?.getString(R.string.app_installed_error_wx), Toast.LENGTH_LONG).show()
                    this.dismiss()
                    return
                }
                platform = SHARE_MEDIA.WEIXIN_CIRCLE
            }
            R.id.im_weixin -> {
                if (!BaseUtils.isAppInstall(Constants.WEIXIN)) {
                    Toast.makeText(mContext!!, mContext?.getString(R.string.app_installed_error_wx), Toast.LENGTH_LONG).show()
                    this.dismiss()
                    return
                }
                platform = SHARE_MEDIA.WEIXIN
            }
            R.id.im_cancel -> {

            }
        }
        shareContent(platform)
        this.dismiss()
    }

    private fun shareContent(platform: SHARE_MEDIA?) {
        saveShareUI2Bitmap()
        platform?.let {
            when (platform) {
                SHARE_MEDIA.QQ -> {
                    val send = Intent()
                    send.action = Intent.ACTION_SEND
                    send.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile))
                    send.type = "image/*"
                    send.setClassName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity") //微信朋友圈，仅支持分享图片
                    mContext?.startActivityForResult(send, WX_QUEST)
                }
                SHARE_MEDIA.WEIXIN_CIRCLE -> {
                    val send = Intent()
                    send.action = Intent.ACTION_SEND
                    send.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile))
                    send.type = "image/*"
                    send.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI") //微信朋友圈，仅支持分享图片
                    mContext?.startActivityForResult(send, WX_QUEST)
                }
                SHARE_MEDIA.WEIXIN -> {
                    val send = Intent()
                    send.action = Intent.ACTION_SEND
                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(applicationContext,
                                "com.lhzw.bluetooth.fileprovider", shareFile!!.absoluteFile) //这个是版本大于Android7.0（包含）临时访问文件，没有这个会报异常
                    } else {
                        Uri.fromFile(shareFile)
                    }
                    send.putExtra(Intent.EXTRA_STREAM, uri)
                    send.type = "image/*"
                    send.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")  //微信朋友，仅支持分享图片
                    mContext?.startActivityForResult(send, WX_QUEST);
                }
                else -> {
                }
            }
        }

    }

    private val shareListener = object : UMShareListener {
        override fun onResult(share_media: SHARE_MEDIA?) {
            Log.e("Tag", "share complete")
        }

        override fun onCancel(share_media: SHARE_MEDIA?) {
            Log.e("Tag", "share onCancel")
        }

        override fun onError(share_media: SHARE_MEDIA?, throwable: Throwable?) {
            Log.e("Tag", "share onError")
        }

        override fun onStart(share_media: SHARE_MEDIA) {
            Log.e("Tag", "share onStart")
        }
    }

    private fun saveShareUI2Bitmap() {
        if (shareFile == null) {
            shareFile = BaseUtils.saveBitmapFile(shareBitmap!!, path)!!
        }
    }

    fun showDialog() {
        this.show()
    }

    fun getView() = convertview

    private fun onDetroy() {
        mContext = null
        convertview = null
        path = null
        shareFile = null
        shareBitmap = null
    }
}
