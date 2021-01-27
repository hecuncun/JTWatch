package com.lhzw.bluetooth.net.rxnet.core;

import android.text.TextUtils;

import com.lhzw.bluetooth.constants.Constants;
import com.lhzw.bluetooth.net.rxnet.callback.DownloadListener;
import com.lhzw.bluetooth.net.rxnet.utils.LogUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Date： 2020/6/2 0002
 * Time： 10:06
 * Created by xtqb.
 */
public class RetrofitFactory {
    private final int TIME_OUT_SECNOD = 10;
    private OkHttpClient.Builder mBuilder;

    private Retrofit getDownloadRetrofit(String token, DownloadListener downloadListener) {
        Interceptor headerInterceptor = chain -> {
            Request originalRequest = chain.request();
            Request.Builder requestBuilder = originalRequest.newBuilder()
                    .addHeader("Accept-Encoding", "gzip")
                    .addHeader("x-access-token", token)
                    .method(originalRequest.method(), originalRequest.body());
            Request request = requestBuilder.build();
            return chain.proceed(request);
        };

        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(message -> {
            if (!TextUtils.isEmpty(message)) {
                LogUtils.d(message);
            }
        });
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        if (null == mBuilder) {
            mBuilder = new OkHttpClient.Builder()
                    .connectTimeout(TIME_OUT_SECNOD, TimeUnit.SECONDS)
                    .readTimeout(TIME_OUT_SECNOD, TimeUnit.SECONDS)
                    .writeTimeout(TIME_OUT_SECNOD, TimeUnit.SECONDS)
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(logInterceptor);

        }
        mBuilder.addInterceptor(new DownloadInterceptor(downloadListener));

        return new Retrofit.Builder()
                .baseUrl(Constants.IP_ADD)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mBuilder.build())
                .build();
    }

    /**
     * 取消网络请求
     */
    public void cancel(Disposable d) {
        if (null != d && !d.isDisposed()) {
            d.dispose();
        }
    }

    /**
     * 下载文件请求
     */
    public void downloadFile(String token, String url, long startPos, DownloadListener downloadListener, Observer<ResponseBody> observer) {
        getDownloadRetrofit(token, downloadListener).create(BaseApi.class).downloadFile("bytes=" + startPos + "-", url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

}
