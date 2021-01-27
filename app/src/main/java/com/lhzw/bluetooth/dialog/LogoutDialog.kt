package com.lhzw.bluetooth.dialog

import android.content.Context
import android.graphics.Color
import android.view.View
import com.flyco.dialog.utils.CornerUtils
import com.flyco.dialog.widget.internal.BaseAlertDialog
import com.lhzw.bluetooth.R

import kotlinx.android.synthetic.main.dialog_logout.*

/**
 * Created by heCunCun on 2019/12/4
 */
class LogoutDialog(context: Context):BaseAlertDialog<LogoutDialog>(context) {
    override fun onCreateView(): View {
        widthScale(0.85f)
        val view =View.inflate(context, R.layout.dialog_logout,null)
        view.setBackgroundDrawable(
                CornerUtils.cornerDrawable(Color.parseColor("#FFFFFF"), dp2px(5f).toFloat()))
        return view
    }

    override fun setUiBeforShow() {
       tv_cancel.setOnClickListener { dismiss() }
    }
    fun setConfirmListener(listener:View.OnClickListener){
        tv_confirm.setOnClickListener(listener)
    }

    fun setTitle(title:String){
        tv_title.text=title
    }
}