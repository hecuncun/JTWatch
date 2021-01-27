package com.lhzw.bluetooth.uitls

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.support.annotation.RequiresApi
import com.lhzw.bluetooth.application.App.Companion.context
import com.orhanobut.logger.Logger
import java.util.*


/**
 * Created by heCunCun on 2020/7/13
 *
 *
 * 配置权限https://juejin.im/post/5dfaeccbf265da33910a441d#heading-2
 *`
 * <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
 */
object KeepLiveUtil {
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        return isIgnoring
    }

    @SuppressLint("BatteryLife")
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestIgnoreBatteryOptimizations() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.flags=FLAG_ACTIVITY_NEW_TASK
            Logger.e("请求白名单授权")
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("请求白名单授权遇到问题${e.localizedMessage}")
        }

    }

    /**
     * 跳转到指定应用的首页
     */
    fun showActivity(packageName: String) {
        val intent: Intent = context.packageManager.getLaunchIntentForPackage(packageName)
        context.startActivity(intent)
    }

    /**
     * 跳转到指定应用的指定页面
     */
    fun showActivity(packageName: String, activityDir: String) {
        val intent = Intent()
        intent.component = ComponentName(packageName, activityDir)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    //华为
    fun isHuawei(): Boolean {
        return if (Build.BRAND != null) {
            Build.BRAND.toLowerCase(Locale.ROOT) == "huawei" || Build.BRAND.toLowerCase(Locale.ROOT) == "honor"
        } else {
            false
        }
    }

    fun goHuaweiSetting() {
        try {
            showActivity("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
        } catch (e: Exception) {
            showActivity("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity")
        }
    }

    //小米
    fun isXiaomi(): Boolean {
        return Build.BRAND != null && Build.BRAND.toLowerCase(Locale.ROOT) == "xiaomi"
    }

    fun goXiaomiSetting() {
        showActivity("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
    }

    //OPPO
    fun isOPPO(): Boolean {
        return Build.BRAND != null && Build.BRAND.toLowerCase(Locale.ROOT) == "oppo"
    }

    fun goOPPOSetting() {
        try {
            showActivity("com.coloros.phonemanager")
        } catch (e1: Exception) {
            try {
                showActivity("com.oppo.safe")
            } catch (e2: Exception) {
                try {
                    showActivity("com.coloros.oppoguardelf")
                } catch (e3: Exception) {
                    showActivity("com.coloros.safecenter")
                }
            }
        }
    }

    // VIVO
    fun isVIVO(): Boolean {
        return Build.BRAND != null && Build.BRAND.toLowerCase(Locale.ROOT) == "vivo"
    }

    fun goVIVOSetting() {
        showActivity("com.iqoo.secure")
    }

    //魅族
    fun isMeizu(): Boolean {
        return Build.BRAND != null && Build.BRAND.toLowerCase(Locale.ROOT) == "meizu"
    }

    fun goMeizuSetting() {
        showActivity("com.meizu.safe")
    }

    //三星
    fun isSamsung(): Boolean {
        return Build.BRAND != null && Build.BRAND.toLowerCase(Locale.ROOT) == "samsung"
    }

    fun goSamsungSetting() {
        try {
            showActivity("com.samsung.android.sm_cn")
        } catch (e: Exception) {
            showActivity("com.samsung.android.sm")
        }
    }

    //乐视
    fun isLeTV(): Boolean {
        return Build.BRAND != null && Build.BRAND.toLowerCase(Locale.ROOT) == "letv"
    }

    fun goLetvSetting() {
        showActivity("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")
    }

    //锤子
    fun isSmartisan(): Boolean {
        return Build.BRAND != null && Build.BRAND.toLowerCase(Locale.ROOT) == "smartisan";
    }

    fun goSmartisanSetting() {
        showActivity("com.smartisanos.security")
    }

}