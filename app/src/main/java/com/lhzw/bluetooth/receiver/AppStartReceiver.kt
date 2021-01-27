package com.lhzw.bluetooth.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.lhzw.bluetooth.ui.activity.SplashActivity


/**
 * Date： 2020/7/9 0009
 * Time： 15:54
 * Created by xtqb.
 */
class AppStartReceiver : BroadcastReceiver() {
    override fun onReceive(mContext: Context?, intent: Intent?) {
        mContext?.let {
            val action = intent!!.action
            //取得AppStartReceiver所在的App的包名
            val localPkgName = it.packageName
            val data: Uri = intent!!.data
            //取得安装的Apk的包名，只在该app覆盖安装后自启动
            val installedPkgName: String = data.schemeSpecificPart
            if ((action == Intent.ACTION_PACKAGE_ADDED || action == Intent.ACTION_PACKAGE_REPLACED) && installedPkgName == localPkgName) {
                //app启动的Activity
                val launchIntent = Intent(it, SplashActivity::class.java)
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                it.startActivity(launchIntent)
            }
        }
    }
}