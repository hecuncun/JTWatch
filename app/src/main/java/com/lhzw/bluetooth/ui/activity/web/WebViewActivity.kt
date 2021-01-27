package com.lhzw.bluetooth.ui.activity.web

import android.widget.LinearLayout
import com.just.agentweb.AgentWeb
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.base.BaseActivity
import kotlinx.android.synthetic.main.activity_webview.*

/**
 * Created by heCunCun on 2020/10/20
 */
class WebViewActivity : BaseActivity() {
    private var mAgentWeb: AgentWeb? = null

    private var preWeb: AgentWeb.PreAgentWeb? = null

    override fun attachLayoutRes(): Int = R.layout.activity_webview

    override fun initData() {
        tv_title.text="用户协议和隐私政策"
        val url = intent.getStringExtra("url")
        //加载网页
        mAgentWeb = preWeb?.go(url)

    }

    override fun onBackPressed() {
        mAgentWeb?.let { web ->
            if (web.webCreator.webView.canGoBack()) {
                web.webCreator.webView.goBack()
            } else {
                finish()
            }
        }
        super.onBackPressed()
    }

    override fun initView() {
       // toolbar_title.text="用户协议和隐私政策"
        preWeb = AgentWeb.with(this)
                .setAgentWebParent(webcontent, LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
    }

    override fun initListener() {
        iv_back.setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        mAgentWeb?.webLifeCycle?.onPause()
        super.onPause()
    }

    override fun onResume() {
        mAgentWeb?.webLifeCycle?.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        mAgentWeb?.webLifeCycle?.onDestroy()
        super.onDestroy()
    }
}