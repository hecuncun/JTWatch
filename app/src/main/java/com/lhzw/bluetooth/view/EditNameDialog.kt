package com.lhzw.bluetooth.view

import android.content.Context
import android.graphics.Color
import android.view.View
import com.flyco.dialog.utils.CornerUtils
import com.flyco.dialog.widget.internal.BaseAlertDialog
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.uitls.ToastUtils
import kotlinx.android.synthetic.main.dialog_edit_name.*

/**
 * Created by heCunCun on 2019/12/4
 */
class EditNameDialog(context: Context) : BaseAlertDialog<EditNameDialog>(context) {
    override fun onCreateView(): View {
        widthScale(0.85f)
        val view = View.inflate(context, R.layout.dialog_edit_name, null)
        view.setBackgroundDrawable(
                CornerUtils.cornerDrawable(Color.parseColor("#FFFFFF"), dp2px(5f).toFloat()))
        return view
    }

    override fun setUiBeforShow() {
        tv_cancel.setOnClickListener { dismiss() }
        tv_confirm.setOnClickListener {
            if (et_name.text.toString().trim().isNotEmpty()) {
                mOnConfirmListener?.onConfirm(et_name.text.toString().trim())
            } else {
                ToastUtils.toastError("请输入昵称")
            }
        }
    }

  interface OnConfirmListener{
      fun onConfirm(name:String)
  }

    private var mOnConfirmListener : OnConfirmListener? =null

    fun setOnConfirmListener(onConfirmListener: OnConfirmListener){
        mOnConfirmListener=onConfirmListener
    }



}