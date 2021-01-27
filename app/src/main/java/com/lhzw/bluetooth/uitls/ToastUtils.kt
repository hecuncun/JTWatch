package com.lhzw.bluetooth.uitls

import com.lhzw.bluetooth.application.App
import com.zia.toastex.ToastEx

/**
 *
@author：created by xtqb
@description:
@date : 2019/12/3 10:27
 *
 */
object ToastUtils {
    /**
     * 选中 成功
     */
    fun toastSuccess(msg: String) {
        ToastEx.Config.reset()//恢复至默认带动画的效果
        ToastEx.success(App.context, msg).show()
    }

    /**
     *  错误提示
     */
    fun toastError(msg: String) {
        ToastEx.Config.reset()//恢复至默认带动画的效果
        ToastEx.error(App.context, msg).show()
    }

    /**
     * 警告提示
     */
    fun toastWarning(msg: String) {
        ToastEx.Config.reset()
        ToastEx.warning(App.context, msg).show()
    }

}