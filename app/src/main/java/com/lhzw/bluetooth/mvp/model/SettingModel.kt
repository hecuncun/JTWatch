package com.lhzw.bluetooth.mvp.model

import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.base.BaseModel
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.bean.WatchInfoBean
import com.lhzw.bluetooth.bean.net.ApkBean
import com.lhzw.bluetooth.bean.net.BaseBean
import com.lhzw.bluetooth.bean.net.FirmBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.mvp.contract.SettingContract
import com.lhzw.bluetooth.net.CallbackListObserver
import com.lhzw.bluetooth.net.SLMRetrofit
import com.lhzw.bluetooth.net.ThreadSwitchTransformer
import org.litepal.LitePal
import org.litepal.extension.findAll

/**
 * Created by hecuncun on 2019/11/13
 */
class SettingModel : BaseModel(), SettingContract.Model {

    override fun getPersonalInfo(): PersonalInfoBean? {
        val list = LitePal.findAll<PersonalInfoBean>()
        return list[0]
    }

    override fun queryWatchData(): List<WatchInfoBean>? {
        return CommOperation.query(WatchInfoBean::class.java);
    }

    override fun getLatestApk(body: (apk: ApkBean?) -> Unit) {
        val response = SLMRetrofit.getInstance().getApi()?.getLatestApk(App.instance.packageName)
        response?.compose(ThreadSwitchTransformer())?.subscribe(object : CallbackListObserver<BaseBean<MutableList<ApkBean>>?>() {
            override fun onSucceed(bean: BaseBean<MutableList<ApkBean>>?) {
                bean?.let {
                    if (it.isSuccessed()) {
                        val beans = it.getData()
                        beans?.let {
                            if (beans.size > 0) {
                                body(beans[0])
                            } else {
                                body(null)
                            }
                        }
                    }
                }
            }

            override fun onFailed() {
                body(null)
            }
        })
    }

    override fun getLatestFirm(body: (firm: FirmBean?) -> Unit) {
        val response = SLMRetrofit.getInstance().getApi()?.getLatestFirm(Constants.WATCH_TYPE)
        response?.compose(ThreadSwitchTransformer())?.subscribe(object : CallbackListObserver<BaseBean<MutableList<FirmBean>>?>() {
            override fun onSucceed(bean: BaseBean<MutableList<FirmBean>>?) {
                bean?.let {
                    if (it.isSuccessed()) {
                        val beans = it.getData()
                        beans?.let {
                            if (beans.size > 0) {
                                body(beans[0])
                            } else {
                                body(null)
                            }
                        }
                    }
                }
            }

            override fun onFailed() {
                body(null)
            }
        })
    }
}