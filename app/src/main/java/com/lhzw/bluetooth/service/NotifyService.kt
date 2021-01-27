package com.lhzw.bluetooth.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.NotificationEvent
import com.lhzw.bluetooth.uitls.Preference
import com.orhanobut.logger.Logger

/**
 * Created by heCunCun on 2020/1/10
 */
class NotifyService :NotificationListenerService() {
    private var connectState: Boolean by Preference(Constants.CONNECT_STATE, false)

    override fun onListenerConnected() {
        Log.e("NotifyService","通知服务已连接 onListenerConnected....")
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        Log.e("NotifyService","通知服务断开连接 onListenerConnected....")
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
     private var temp_mms=""
    override fun onNotificationPosted(sbn: StatusBarNotification?, rankingMap: RankingMap?) {//通知来时调用
        super.onNotificationPosted(sbn, rankingMap)
        //判断来源
        Logger.e("onNotificationPosted收到通知 ID=${sbn?.id}<==>tickerText==${sbn?.notification?.tickerText}<==>packageName==${sbn?.packageName}")
        if (connectState){//处于连接状态就推送消息
            sbn?.let {
                if (sbn.notification.tickerText!=null){
                    if(sbn.packageName==Constants.MMS){
                        if (temp_mms==sbn.notification.tickerText.toString()){//过滤掉重复短信
                            return
                        }
                        temp_mms=sbn.notification.tickerText.toString()
                        Logger.e("NotifyService发送来短信通知给手表")
                    }
                    RxBus.getInstance().post("notification", NotificationEvent(sbn.id,sbn.notification.tickerText.toString(),sbn.packageName))
                }

            }
        }


    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap) {//通知移除调用
        super.onNotificationRemoved(sbn, rankingMap)
       // Logger.e("onNotificationRemoved收到通知 ID=${sbn.id}<==>tickerText==${sbn.notification.tickerText}<==>packageName==${sbn.packageName}")
    }
}