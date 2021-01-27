package com.lhzw.bluetooth.base

import io.reactivex.disposables.Disposable

/**
 * Created by hecuncun on 2019/11/12
 */
interface IModel {
    fun addDisposable(disposable: Disposable?)//添加订阅事件
    fun onDetach()//解除订阅
}