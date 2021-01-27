package com.lhzw.bluetooth.net

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Date： 2020/6/2 0002
 * Time： 10:04
 * Created by xtqb.
 */
class ThreadSwitchTransformer<T> : ObservableTransformer<T?, T?> {
    override fun apply(upstream: Observable<T?>): ObservableSource<T?> {
        return upstream.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}