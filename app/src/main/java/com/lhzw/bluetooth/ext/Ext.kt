package com.lhzw.bluetooth.ext

import android.app.Activity
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.widget.TextView
import android.widget.Toast
import com.lhzw.bluetooth.R
import com.lhzw.bluetooth.application.App
import java.text.SimpleDateFormat
import java.util.*


/**
 * 扩展函数
 */

fun Fragment.showToast(content: String) {
    Toast.makeText(App.context, content, Toast.LENGTH_SHORT).show()
}

fun Context.showToast(content: String) {
    Toast.makeText(App.context, content, Toast.LENGTH_SHORT).show()
}

fun Activity.showSnackMsg(msg: String) {
    val snackbar = Snackbar.make(this.window.decorView, msg, Snackbar.LENGTH_SHORT)
    val view = snackbar.view
    view.findViewById<TextView>(R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.white))
    snackbar.show()
}

fun Fragment.showSnackMsg(msg: String) {
    this.activity ?: return
    val snackbar = Snackbar.make(this.activity!!.window.decorView, msg, Snackbar.LENGTH_SHORT)
    val view = snackbar.view
    view.findViewById<TextView>(R.id.snackbar_text).setTextColor(ContextCompat.getColor(this.activity!!, R.color.white))
    snackbar.show()
}

fun TextView.underline() {
    if (text.isNotEmpty()) {
        val spannableString = SpannableString(text.toString().trim())
        val underlineSpan = UnderlineSpan()
        spannableString.setSpan(
                underlineSpan,
                0,
                spannableString.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        text = spannableString
    }

}

/**
 * 格式化当前日期
 */
fun formatCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    return sdf.format(Date())
}

/**
 * String 转 Calendar
 */
fun String.stringToCalendar(): Calendar {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val date = sdf.parse(this)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar
}