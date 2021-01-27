package com.lhzw.bluetooth.base

/**
 * Created by hecuncun on 2019/11/12
 */
interface IPresenter<in V:IView> {
    /**
     * 绑定 View
     */
    fun attachView(mView: V)

    /**
     * 解绑 View
     */
    fun detachView()
}