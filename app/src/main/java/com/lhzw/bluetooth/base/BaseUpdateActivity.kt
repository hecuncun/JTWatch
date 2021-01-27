package com.lhzw.bluetooth.base

import com.lhzw.bluetooth.bus.RxBus
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.mvp.contract.UpdateContract
import com.lhzw.bluetooth.mvp.presenter.MainUpdatePresenter
import com.lhzw.bluetooth.uitls.Preference
import com.lhzw.bluetooth.widget.LoadingView
import com.lhzw.kotlinmvp.presenter.BaseIPresenter
import kotlinx.android.synthetic.main.dialog_progress_bar.*
import java.text.SimpleDateFormat

/**
 * Date： 2020/7/6 0006
 * Time： 15:04
 * Created by xtqb.
 */
abstract class BaseUpdateActivity<T : BaseIPresenter<UpdateContract.IView>> : BaseActivity(), UpdateContract.IView {
    protected var mPresenter: MainUpdatePresenter? = null
    private var loadingView: LoadingView? = null
    protected var apk_update_time: String? by Preference(Constants.APK_UPDATE_TIME, "")
    protected var firm_update_time: String? by Preference(Constants.FIRM_UPDATE_TIME, "")
    protected val sdf: SimpleDateFormat = SimpleDateFormat("yyyy年MM月dd日更新")
    override fun initView() {
        mPresenter = getMainPresent() as MainUpdatePresenter
        mPresenter?.let {
            it.attachView(this)
            it?.onAttach()
        }
        RxBus.getInstance().register(this)
    }

    protected fun showLoadingView(note: String) {
        if (loadingView == null) {
            loadingView = LoadingView(this@BaseUpdateActivity)
        }
        loadingView?.setCancelable(false)
        loadingView?.setLoadingTitle(note)
        loadingView?.show()
    }

    protected fun cancelLoadingView() {
        loadingView?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingView?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            loadingView = null
        }
        mPresenter?.let {
            it.onDettach()
            it.detachView()
            mPresenter = null
        }
        RxBus.getInstance().unregister(this)
    }

    abstract fun getMainPresent(): T?
}