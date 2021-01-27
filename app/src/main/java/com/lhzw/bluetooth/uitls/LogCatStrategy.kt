package com.lhzw.bluetooth.uitls

import android.util.Log

import com.orhanobut.logger.LogStrategy

/**
 * Created by hecuncun on 2019/8/15
 * 日志格式化
 */
class LogCatStrategy : LogStrategy {

    private var last: Int = 0

    override fun log(priority: Int, tag: String?, message: String) {
        Log.println(priority, randomKey() + tag, message)
    }

    private fun randomKey(): String {
        var random = (10 * Math.random()).toInt()
        if (random == last) {
            random = (random + 1) % 10
        }
        last = random
        return random.toString()
    }


}
