package com.lhzw.kotlinmvp.presenter

import com.lhzw.bluetooth.base.BaseIView


/**
 *
@author：created by xtqb
@description: Presenter与View进行绑定
@date : 2019/11/12 10:04
 *
 */
open class BaseIPresenter<V : BaseIView> {
    var mView: V? = null

    /**
     * 绑定view，一般在初始化中调用该方法
     *
     * @param view view
     */
    fun attachView(view: V) {
        this.mView = view
    }


    /**
     * 解除绑定view，一般在onDestroy中调用
     */
    open fun detachView() {
        this.mView = null
    }

    /**
     * View是否绑定
     *
     * @return
     */
    fun isViewAttached(): Boolean {
        return mView == null
    }

}

