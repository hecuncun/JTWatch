package com.lhzw.bluetooth.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Vibrator
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_scan_qrcode.*

/**
 * Created by hecuncun on 2020-6-10
 * 二维码扫描页
 */
class ScanQRCodeActivity : BaseActivity(), QRCodeView.Delegate {
    override fun attachLayoutRes(): Int = R.layout.activity_scan_qrcode
    private var isOpen = false
    override fun initData() {
        iv_light.setOnClickListener {
            isOpen = !isOpen
            if (isOpen) {
                zxingview.openFlashlight()
            } else {
                zxingview.closeFlashlight()
            }

        }

        iv_finish.setOnClickListener {
            finish()
        }
    }

    override fun initView() {
        zxingview.setDelegate(this)
        zxingview.changeToScanQRCodeStyle()
    }

    override fun initListener() {

    }

    override fun onScanQRCodeSuccess(result: String?) {
        Logger.e("result==$result")
        vibrate()
        val resultIntent = Intent()
        resultIntent.putExtra("result", result)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun vibrate() {
        val vibrator =
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(200)
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {

    }

    override fun onScanQRCodeOpenCameraError() {

    }

    override fun onStart() {
        super.onStart()
        zxingview.startCamera()
        zxingview.startSpotAndShowRect()
    }

    override fun onStop() {
        zxingview.stopCamera()
        super.onStop()
    }

    override fun onDestroy() {
        zxingview.onDestroy()
        super.onDestroy()
    }

}