package com.lhzw.bluetooth.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.telephony.SmsMessage
import android.text.TextUtils
import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.event.NotificationEvent
import com.lhzw.bluetooth.uitls.PhoneUtil
import com.lhzw.bluetooth.uitls.Preference
import com.orhanobut.logger.Logger

/**
 * Created by heCunCun on 2020/4/7
 * 来电  短信  服务信息通知
 */
class SmsAndPhoneService : Service() {
    private var connectState: Boolean by Preference(Constants.CONNECT_STATE, false)
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        connectState=false
        Logger.e("SmsAndPhoneService ====onCreate")
        val localIntentFilter = IntentFilter()
        localIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED")
        localIntentFilter.addAction("android.intent.action.PHONE_STATE")
        registerReceiver(NLServerReceiver, localIntentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.e("SmsAndPhoneService--------onStartCommand--------->")
        return START_STICKY
    }

    override fun onDestroy() {
        Logger.e("SmsAndPhoneService----onDestroy----unregisterReceiver--------->$NLServerReceiver")
        unregisterReceiver(NLServerReceiver)
        super.onDestroy()
    }

    private val NLServerReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(paramContext: Context, paramIntent: Intent) {
            var phoneNumber = ""
            var name = ""
            val action = paramIntent.action ?: return
            if (action == "android.provider.Telephony.SMS_RECEIVED") {
                Logger.e("SmsAndPhoneService服务收到短息==>Telephony.SMS_RECEIVED")
                var msg = ""
                //---接收传入的消息---
                val bundle = paramIntent.extras
                var msgs: Array<SmsMessage?>? = null
                if (bundle != null) { //---查询到达的消息---
                    val pdus = bundle["pdus"] as Array<Any>
                    msgs = arrayOfNulls(pdus.size)
                    for (i in msgs.indices) {
                        msgs[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                        if (i == 0) { //---获取发送者手机号---
                            phoneNumber = msgs[i]?.getOriginatingAddress().toString().trim()
                            name=PhoneUtil.getDisplayNameByPhone1(paramContext, phoneNumber).trim()
                        }
                        //---获取消息内容---
                        msg = msgs[i]?.getMessageBody().toString().trim()
                    }
                    //---显示SMS消息---
                    Logger.e("SmsAndPhoneService服务收到短息==>$name($phoneNumber):$msg")
                    val message = name+phoneNumber+":"+msg
                    if (connectState) {
                        //Logger.e("SmsAndPhoneService发送来短信通知给手表==>$message")
                       // RxBus.getInstance().post("notification", NotificationEvent(100, message, Constants.MMS))
                    }
                }

            }
            if (action == "android.intent.action.PHONE_STATE") {
                val state = paramIntent.getStringExtra("state")
                val incoming_number = paramIntent.getStringExtra("incoming_number")
                Logger.e("state==$state----------incoming_number==$incoming_number")
                if ("RINGING" == state) {
                    if (!TextUtils.isEmpty(incoming_number)) {
                        phoneNumber = incoming_number.replace("-", "").replace(" ", "")
                        try {
                            name = PhoneUtil.getDisplayNameByPhone1(paramContext, phoneNumber)
                            Logger.e("来电名字==$name")
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                        Logger.e("SmsAndPhoneService收到来电phoneNumber==$phoneNumber===name==$name")
                        if (connectState) {
                            RxBus.getInstance().post("notification", NotificationEvent(101, name + phoneNumber, Constants.CALL_COMING))
                        }
                    }
                }
                if ("IDLE" == state) { //挂断
                }
                return
            }
        }
    }
}