package com.lhzw.bluetooth.application

//import com.uuzuche.lib_zxing.activity.ZXingLibrary

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.multidex.MultiDexApplication
import android.util.Log
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.service.BleConnectService
import com.lhzw.bluetooth.service.BlutoothService
import com.lhzw.bluetooth.service.SmsAndPhoneService
import com.lhzw.bluetooth.uitls.BaseUtils
import com.lhzw.bluetooth.uitls.LogCatStrategy
import com.lhzw.bluetooth.uitls.Preference
import com.orhanobut.logger.*
import com.simple.spiderman.SpiderMan
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.tencent.bugly.crashreport.CrashReport
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import org.litepal.LitePal
import kotlin.properties.Delegates


/**
 *
@author：created by xtqb
@description:
@date : 2019/11/6 8:51
 *
 */
class App : MultiDexApplication() {

    private var refWatcher: RefWatcher? = null

    private var http_token: String? by Preference(Constants.HTTP_TOOKEN, "")

    companion object {

        private val TAG = "App"

        private var activityContext: Activity? = null

        private var isSynState = false

        var context: Context by Delegates.notNull()
            private set//  对于属性context，如果你想改变访问的可见性，但是又不想改变它的默认实现，那么你就可以定义set和get但不进行实现。

        lateinit var instance: App

        fun getRefWatcher(context: Context): RefWatcher? {
            val app = context.applicationContext as App
            return app.refWatcher
        }

        fun getActivityContext(): Activity? {
            return activityContext
        }

        fun setActivityContext(activityContext: Activity?) {
            this.activityContext = activityContext
        }

        fun setSynState(state: Boolean) {
            isSynState = state
        }

        fun isSynState() = isSynState
    }

    fun getToken() = http_token

    override fun onCreate() {
        super.onCreate()
        instance = this
        context = applicationContext
        SpiderMan.init(this)//奔溃日志
        refWatcher = setupLeakCanary()
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
        try{
            // 启动服务

            if (!BaseUtils.isServiceRunning(Constants.SERVICE_PACKAGE)) {
                startService(Intent(context, BlutoothService::class.java))
                Log.e("tag","Application蓝牙服务")
            }
            //启动蓝牙连接服务
            if (!BaseUtils.isServiceRunning(Constants.BLE_CONNECT_SERVICE_PACKAGE)) {
                startService(Intent(context, BleConnectService::class.java))
                Log.e("tag","Application启动连接服务")
            }
            //启动电话/短信监听服务
            if (!BaseUtils.isServiceRunning(Constants.SMS_AND_PHONE_SERVICE_PACKAGE)) {
                startService(Intent(context, SmsAndPhoneService::class.java))
                Log.e("tag","Application启动连接服务SmsAndPhoneService")
            }

        }catch (e:Exception){
            Log.e("tag","APP第一次启动连接服务  异常")
        }

        initLoggerConfig()
        //初始化数据库
        LitePal.initialize(context)
        //初始化二维码扫描
        // ZXingLibrary.initDisplayOpinion(this)
        //设置LOG开关，默认为false
        UMConfigure.setLogEnabled(true);
        // 分享权限
        UMConfigure.init(this, "5dde2cda0cafb206e4000262"
                , getString(R.string.app_name), UMConfigure.DEVICE_TYPE_PHONE, "")

        PlatformConfig.setWeixin("wxdc1e388c3822c80b", "3baf1193c85774b3fd9d18447d76cab0");
        PlatformConfig.setSinaWeibo("3921700954", "04b48b094faeb16683c32669824ebdad", "http://sns.whalecloud.com")
        PlatformConfig.setQQZone("1110222616", "Q59lLBkXbLssHlK")

        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);

        //BUGly初始化
        CrashReport.initCrashReport(applicationContext, "9493728e5c", false)
    }

    private fun setupLeakCanary(): RefWatcher? {
        return if (LeakCanary.isInAnalyzerProcess(this)) {
            RefWatcher.DISABLED
        } else LeakCanary.install(this)
    }

    /**
     * 初始化LOGGER配置
     */
    private fun initLoggerConfig() {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
                .logStrategy(LogCatStrategy())
                .tag("YeWan")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
        val csvFormat = CsvFormatStrategy.newBuilder()
                .tag("BLUETOOTH")
                .build()
        Logger.addLogAdapter(DiskLogAdapter(csvFormat))
    }


    private val mActivityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.d(TAG, "onCreated: " + activity.componentName.className)
        }

        override fun onActivityStarted(activity: Activity) {
            Log.d(TAG, "onStart: " + activity.componentName.className)
        }

        override fun onActivityResumed(activity: Activity) {

        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            Log.d(TAG, "onDestroy: " + activity.componentName.className)
        }
    }
}
