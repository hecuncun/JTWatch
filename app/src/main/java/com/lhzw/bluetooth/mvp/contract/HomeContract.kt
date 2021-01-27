package com.lhzw.bluetooth.mvp.contract

import com.lhzw.bluetooth.base.IModel
import com.lhzw.bluetooth.base.IPresenter
import com.lhzw.bluetooth.base.IView

/**
 * Created by hecuncun on 2019/11/13
 */
interface HomeContract{

    interface View:IView{
    }

    interface Presenter : IPresenter<View> {

    }

    interface Model : IModel {

    }
}