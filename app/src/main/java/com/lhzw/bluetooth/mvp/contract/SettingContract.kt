package com.lhzw.bluetooth.mvp.contract

import android.content.Context
import com.lhzw.bluetooth.base.IModel
import com.lhzw.bluetooth.base.IPresenter
import com.lhzw.bluetooth.base.IView
import com.lhzw.bluetooth.bean.PersonalInfoBean
import com.lhzw.bluetooth.bean.WatchInfoBean
import com.lhzw.bluetooth.bean.net.ApkBean
import com.lhzw.bluetooth.bean.net.FirmBean

/**
 * Created by hecuncun on 2019/11/13
 */
interface SettingContract {

    //传到UI页面
    interface View : IView {
        fun getPersonalInfoSuccess(data: PersonalInfoBean?)

        fun refleshUpdateState(state: Boolean)
    }


    //Presenter,Model接口的方法名最好保持一致
    interface Presenter : IPresenter<View> {
        fun getPersonalInfo()

        fun checkUpdate(mContext: Context)
    }

    interface Model : IModel {
        fun getPersonalInfo(): PersonalInfoBean?

        fun queryWatchData(): List<WatchInfoBean>?

        fun getLatestApk(body: (apk: ApkBean?) -> Unit)

        fun getLatestFirm(body: (firm: FirmBean?) -> Unit)
    }
}