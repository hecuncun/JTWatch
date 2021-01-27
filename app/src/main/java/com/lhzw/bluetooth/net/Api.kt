package com.lhzw.bluetooth.net

import com.lhzw.bluetooth.bean.MsgVerifyBean
import com.lhzw.bluetooth.bean.net.*
import io.reactivex.Observable;
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Date： 2020/6/2 0002
 * Time： 10:06
 * Created by xtqb.
 */

interface Api {
    /**
     * 登录
     */
    @GET("security/login")
    fun login(@Query("loginName") loginName: String, @Query("password") password: String): Observable<BaseBean<UserInfo<SubJoin>>>

    /**
     * 获取最新 apk 信息
     */
    @GET("apks/latest")
    fun getLatestApk(@Query("packageName") packageName: String): Observable<BaseBean<MutableList<ApkBean>>>

    /**
     * apk 下载
     */
    @GET("attachments/apks/{id}")
    fun downloadApk(@Path("id") id: Long): Observable<Response<ResponseBody>>

    /**
     * 获取最新腕表固件
     */
    @GET("firmware/latest")
    fun getLatestFirm(@Query("model") model: String): Observable<BaseBean<MutableList<FirmBean>>>

    /**
     * 腕表固件 下载
     */
    @GET("attachments/firms/{id}")
    fun downloadDfu(@Path("id") id: Long): Observable<Response<ResponseBody>>

    /**
     * 注册用户信息
     *      verifyCode 不能为空
     */
    @POST(value = "security/insert/bean")
    fun insertUser(@Body user: LoginUser, @Query("verifyCode") verifyCode: String): Observable<BaseBean<String>>

    /**
     *  获取验证码
     *  每个校验码都有自己的生命周期，每个校验码生命周期为5分钟；重复发送，上一个校验码会立即终止生命周期
     *
     */
    @POST(value = "verify/tell_code")
    fun getMsgVerifyCode(@Body bean: MsgVerifyBean): Observable<BaseBean<String>>

    /**
     * 修改密码 对象MsgVerifyBean中的成员变量均不能为空
     *
     */
    @PUT(value = "user/modify")
    fun modifyPassword(@Body bean: MsgVerifyBean): Observable<BaseBean<String>>

}
