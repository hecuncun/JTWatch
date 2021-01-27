package com.lhzw.bluetooth.wxapi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lhzw.bluetooth.constants.Constants
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.io.ByteArrayOutputStream

/**
 *
 * @ProjectName: BluetoothWatch
 * @Author：created by xtqb
 * @CreateDate: 2020/10/21 0021 10:38
 * @Description:
 *
 */
object ShareUtils {
    fun shareImageToWx(mContext: Context, imagePath: String?, state: Int) {
        //创建WXImageObject对象，并设置文件路径
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val imgObj = WXImageObject(bitmap)
        //创建WXMediaMessage对象，并包装创建WXImageObject对象
        val msg = WXMediaMessage()
        msg.mediaObject = imgObj
        //压缩图像
        val thumbBmp = Bitmap.createScaledBitmap(bitmap, 120, 120, true)
        bitmap.recycle() //释放图像所占用的内存资源
        msg.thumbData = bmpToByteArray(thumbBmp, true) //设置缩略图
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction("img")
        req.message = msg
        req.scene = if (state == 0) SendMessageToWX.Req.WXSceneSession else SendMessageToWX.Req.WXSceneTimeline
        req.userOpenId = Constants.USER_ID
        WXAPIFactory.createWXAPI(mContext, Constants.APP_ID).sendReq(req)
    }

    private fun bmpToByteArray(bitmap: Bitmap?, needRecycle: Boolean): ByteArray? {
        val output = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.PNG, 99, output)
        if (needRecycle) bitmap.recycle()
        val result = output.toByteArray()
        try {
            output.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun buildTransaction(type: String?): String? {
        return if (type == null) System.currentTimeMillis().toString() else type + System.currentTimeMillis()
    }
}