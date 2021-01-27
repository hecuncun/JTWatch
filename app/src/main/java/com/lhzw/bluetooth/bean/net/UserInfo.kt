package com.lhzw.bluetooth.bean.net

/**
 * Date： 2020/6/2 0002
 * Time： 10:27
 * Created by xtqb.
 */

class UserInfo<T> {
    private var token: String? = null

    private var user: T? = null

    fun getToken(): String? = token

    fun getInfo() = user

}