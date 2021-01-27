package com.lhzw.bluetooth.bean

/**
 *
 * @ProjectName: BluetoothWatch
 * @Author：created by xtqb
 * @CreateDate: 2020/8/26 0026 16:55
 * @Description:
 *
 *  说明：
 *      获取检验码时，参数 ： verifyCode  和 参数 password 可以为null
 *      修改密码时， 所有参数均不能为空 ！！！
 *
 *      参数 type：
 *              Constants.VERIFY_TYPE_REGISTER 注册
 *              Constants.VERIFY_TYPE_MODIFY_PASSWORD 更改密码
 *
 */
data class MsgVerifyBean(val loginName: String, val tell: String, val verifyCode: String?, val password: String?, val type: Int)