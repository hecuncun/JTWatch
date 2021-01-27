package com.lhzw.bluetooth.base

import android.view.View
import com.lhzw.bluetooth.ext.showToast

/**
 * @desc BaseMvpFragment
 */
@Suppress("UNCHECKED_CAST")

abstract class BaseMvpFragment<in V : IView, P : IPresenter<V>> : BaseFragment(), IView {
//in即java中的<? super T> 意为仅可作为参数传入，传入的参数类型是T或T的子类
//out即java中的<? extends T>意为仅可作为返回值， 返回值类型是T或T的父类
    /**
     * Presenter
     */
    protected var mPresenter: P? = null

    protected abstract fun createPresenter(): P

    override fun initView(view: View) {
        mPresenter = createPresenter()
        mPresenter?.attachView(this as V)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.detachView()
        this.mPresenter = null
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun showError(errorMsg: String) {
        showToast(errorMsg)
    }

    override fun showDefaultMsg(msg: String) {
        showToast(msg)
    }

    override fun showMsg(msg: String) {
        showToast(msg)
    }

}