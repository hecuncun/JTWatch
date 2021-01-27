package com.lhzw.bluetooth.wxapi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.constants.Constants
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.umeng.socialize.weixin.view.WXCallbackActivity


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/28 10:17
 *
 */
class WXEntryActivity : WXCallbackActivity(), IWXAPIEventHandler {
    private var api: IWXAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wxentry)
        api = WXAPIFactory.createWXAPI(App.context, Constants.APP_ID, false)
        api?.handleIntent(intent, this)
        api?.registerApp(Constants.APP_ID)
    }

    override fun onNewIntent(p0: Intent?) {
        super.onNewIntent(p0)
        intent = p0
        api?.handleIntent(intent, this)
    }

    override fun onReq(baseReq: BaseReq?) {}
    override fun onResp(baseResp: BaseResp) {
        when (baseResp.errCode) {
            BaseResp.ErrCode.ERR_USER_CANCEL -> Toast.makeText(this, "分享取消", Toast.LENGTH_LONG).show()
            BaseResp.ErrCode.ERR_OK -> Toast.makeText(this, "分享成功", Toast.LENGTH_LONG).show()
            BaseResp.ErrCode.ERR_AUTH_DENIED -> Toast.makeText(this, "被拒绝", Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this, "未知错误", Toast.LENGTH_LONG).show()
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        api?.unregisterApp()
        api = null
    }
}