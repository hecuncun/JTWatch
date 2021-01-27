package com.lhzw.bluetooth.bean.net

/**
 *
 * @ProjectName: BluetoothWatch
 * @Author：created by xtqb
 * @CreateDate: 2020/8/5 0005 14:02
 * @Description:
 *
 */
data class LoginUser(
        /*  登录账号  要求 4-12字节 a-z A-Z 0-9  */
        val loginName: String,
        /*  要求 2-12字符 */
        val realName: String,
        /*  性别  0 男  1 女  */
        val gender: Int,
        /*  登录密码  要求 6-14字节 a-z A-Z 0-9  */
        val password: String,
        /*  电话   11位电话  */
        val tell: String,
        /*  邮箱   有效邮箱 */
        val email: String)