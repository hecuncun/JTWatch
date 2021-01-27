package com.lhzw.bluetooth.dialog

import android.content.Context
import android.graphics.Color
import android.view.View
import com.flyco.dialog.utils.CornerUtils
import com.flyco.dialog.widget.internal.BaseAlertDialog
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.uitls.Preference

import kotlinx.android.synthetic.main.dialog_logout.*

/**
 * Created by heCunCun on 2019/12/4
 */
class SaveChangeDialog(context: Context) : BaseAlertDialog<SaveChangeDialog>(context) {
    private var infoChanged: Boolean by Preference(Constants.INFO_CHANGE, false)//更改数据未保存
    override fun onCreateView(): View {
        widthScale(0.85f)
        val view = View.inflate(context, R.layout.dialog_save_change, null)
        view.setBackgroundDrawable(
                CornerUtils.cornerDrawable(Color.parseColor("#FFFFFF"), dp2px(5f).toFloat()))
        return view
    }

    override fun setUiBeforShow() {


    }

    fun setConfirmListener(listener: View.OnClickListener) {
        tv_confirm.setOnClickListener(listener)

    }
    fun setCancelListener(listener: View.OnClickListener){
        tv_cancel.setOnClickListener (listener)
    }

    fun setTitle(title: String) {
        tv_title.text = title
    }
}