package com.lhzw.bluetooth.net

import com.google.gson.GsonBuilder
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.uitls.Preference
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Date： 2020/6/2 0002
 * Time： 9:37
 * Created by xtqb.
 */

class SLMRetrofit {
    private var mApi: Api? = null
    private val DEFAULT_TIME_OUT = 20L //超时时间
    private val DEFAULT_READ_TIME_OUT = 20L
    private var http_token: String? by Preference(Constants.HTTP_TOOKEN, "")


    init {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
                .baseUrl(Constants.IP_ADD)
                .client(genericClient())
                .addConverterFactory(GsonConverterFactory.create(gson))  //数据的处理时，只有请求成功后，才能需要解析data的数据,其他时候我们直接抛异常处理 json解析
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//支持RxJava
                .build();
        mApi = retrofit.create(Api::class.java)
    }

    private fun genericClient(): OkHttpClient? {
        val loggingInterceptor = HttpLoggingInterceptor() //该拦截器用于记录应用中的网络请求的信息
        /**
         * 可以通过 setLevel 改变日志级别
         * 共包含四个级别：NONE、BASIC、HEADER、BODY
         *
         * NONE 不记录
         *
         * BASIC 请求/响应行
         * --> POST /greeting HTTP/1.1 (3-byte body)
         * <-- HTTP/1.1 200 OK (22ms, 6-byte body)
         *
         * HEADER 请求/响应行 + 头
         *
         * --> Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * <-- HTTP/1.1 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * BODY 请求/响应行 + 头 + 体
         */
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true) //失败后，是否重新连接，
                //启用Log日志
                .addInterceptor(loggingInterceptor) //添加拦截器  拦截器拿到了request之后，可以对request进行重写，可以添加，移除，替换请求头，也能对response的header进行重写，改变response的body
                .addInterceptor { chain -> //                        CacheControl.Builder builder = new CacheControl.Builder().maxAge(10, TimeUnit.MINUTES);
                    val request: Request = chain.request()
                            .newBuilder() //                                .header("Cache-Control", builder.build().toString())
                            //                                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                            .addHeader("Content-Type", "application/json; charset=UTF-8") //                                .addHeader("Accept-Encoding", "gzip, deflate")
                            //                                .addHeader("Accept-Encoding", "gzip,sdch")
                            .addHeader("Connection", "keep-alive")
                            .addHeader("Accept", "*/*")
                            .addHeader("x-access-token", http_token) // .addHeader("Cookie", cookie)
                            //.addHeader("Authorization","APPCODE " + Constant.OCR_APP_CODE)
                            .build()
                    chain.proceed(request)
                }
                .build()
    }

    /**



     */

    private val mService: SLMRetrofit by lazy {
        SLMRetrofit()
    }


    companion object {

        private val mService: SLMRetrofit by lazy {
            SLMRetrofit()
        }

        fun getInstance(): SLMRetrofit = mService


    }

    fun getApi(): Api? = mApi
}