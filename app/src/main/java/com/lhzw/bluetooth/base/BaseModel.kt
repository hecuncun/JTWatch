package com.lhzw.bluetooth.base

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by hecuncun on 2019/11/12
 */
abstract class BaseModel : IModel, LifecycleObserver {

    private var mCompositeDisposable: CompositeDisposable? = null
    /*CompositeDisposable 是disposable的容器，可以容纳多个disposable
    1、可以快速解除所有添加的Disposable类.
    2、每当我们得到一个Disposable时就调用CompositeDisposable.add()将它添加到容器中,
    在退出的时候, 调用CompositeDisposable.clear() 即可快速解除.
*/
    override fun addDisposable(disposable: Disposable?) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = CompositeDisposable()
        }
        disposable?.let { mCompositeDisposable?.addAll(it) }
    }

    override fun onDetach() {
        unDispose()
    }

    private fun unDispose() {
        mCompositeDisposable?.clear()//保证Activity结束时取消
        mCompositeDisposable = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
    }
}