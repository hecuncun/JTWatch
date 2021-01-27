package com.lhzw.bluetooth.net

import androidx.annotation.NonNull
import io.reactivex.Observer
import io.reactivex.disposables.Disposable


/**
 * Date： 2020/6/2 0002
 * Time： 9:59
 * Created by xtqb.
 */
abstract class CallbackListObserver<T> : Observer<T> {

    override fun onSubscribe(d: Disposable) {
        onStart()
    }

    override fun onError(e: Throwable) {
        onFailed()
    }

    override fun onNext(bean: T) {
        onSucceed(bean)
    }

    override fun onComplete() {}

    /**
     * 请求开始
     */
    protected open fun onStart() {}

    /**
     * 请求成功
     */
    protected abstract fun onSucceed(t: T)


    /**
     * 请求异常
     */
    protected open fun onException(t: Throwable?) {
//        DialogUtil.showLoadingDialog(HzlcApplication.getContext());
    }

    /**
     * 请求错误
     */
    protected abstract fun onFailed()

}