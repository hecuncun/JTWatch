package com.lhzw.bluetooth.bean.net

/**
 * Date： 2020/6/2 0002
 * Time： 10:08
 * Created by xtqb.
 */
class BaseBean<T> {
    private final val SUCCESSED_CODE = "0"

    private var code: String? = ""
    private var message: String? = ""
    private var data: T? = null

    fun getCode(): String? {
        return code
    }

    fun setCode(code: String?) {
        this.code = code
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    fun getData(): T? {
        return data
    }

    fun setData(data: T) {
        this.data = data
    }


    fun isSuccessed(): Boolean {
        return SUCCESSED_CODE.equals(code)
    }

}