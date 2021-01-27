package com.lhzw.bluetooth.view

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import com.lhzw.bluetooth.R
import kotlinx.android.synthetic.main.dialog_progress_bar.*

/**
 *
@author：created by xtqb
@description:
@date : 2020/4/9 11:43
 *
 */
class SyncProgressBar(val context: Activity) : AlertDialog(context) {
    private var progress : Int = 0
    private val PARSER = 0x01
    private val OBTAIN = 0x02
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_progress_bar)
        init()
    }

    private fun init() {
        setCancelable(false)
        progress = 0
        window.decorView.setBackgroundResource(R.color.transparent)
    }

    fun setProgressBarMax(max: Int, state : Int) {
        context?.let {
            progesss.max = max
            progress = 0
            when(state) {
                PARSER ->{
                    tv_progress_content.text = context.getString(R.string.progress_parser_content).replace("@", "${100 * progress / progesss.max}%")
                }
                OBTAIN -> {
                    tv_progress_content.text = context.getString(R.string.progress_update_content).replace("@", "${100 * progress / progesss.max}%")
                }
            }
        }
    }

    // 更新进度条
    fun refleshProgressBar(state : Int) {
        context?.let {
            progress++
            when(state) {
                PARSER ->{
                    tv_progress_content.text = context.getString(R.string.progress_parser_content).replace("@", "${100 * progress / progesss.max}%")
                }
                OBTAIN -> {
                    tv_progress_content.text = context.getString(R.string.progress_update_content).replace("@", "${100 * progress / progesss.max}%")
                }
            }
            progesss.setProgress(progress)
        }
    }

    fun startProgress() {
        progesss.max = 100
        progesss.setProgress(0)
        var counter = 0
        Thread {
            while (counter < 101) {
                counter++
                progesss.setProgress(counter)
                Thread.sleep(50)
            }
        }.start()
    }
}