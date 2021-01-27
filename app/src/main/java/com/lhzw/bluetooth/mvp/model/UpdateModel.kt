package com.lhzw.bluetooth.mvp.model

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.content.FileProvider
import android.util.Log
import com.lhzw.bluetooth.application.App
import com.lhzw.bluetooth.bean.WatchInfoBean
import com.lhzw.bluetooth.bean.net.ApkBean
import com.lhzw.bluetooth.bean.net.BaseBean
import com.lhzw.bluetooth.bean.net.FirmBean
import com.lhzw.bluetooth.constants.Constants
import com.lhzw.bluetooth.db.CommOperation
import com.lhzw.bluetooth.mvp.contract.UpdateContract
import com.lhzw.bluetooth.net.CallbackListObserver
import com.lhzw.bluetooth.net.DownloadTransformer
import com.lhzw.bluetooth.net.SLMRetrofit
import com.lhzw.bluetooth.net.ThreadSwitchTransformer
import com.lhzw.bluetooth.net.rxnet.RxNet
import com.lhzw.bluetooth.net.rxnet.callback.DownloadCallback
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File


/**
 * Date： 2020/7/6 0006
 * Time： 15:09
 * Created by xtqb.
 *
 */
class UpdateModel : UpdateContract.IModel {
    override fun onDettach() {
        // 清空数据
    }

    override fun queryWatchData(): List<WatchInfoBean>? {
        return CommOperation.query(WatchInfoBean::class.java);
    }

    override fun getLatestApk(body: (apk: ApkBean?) -> Unit) {
        val response = SLMRetrofit.getInstance().getApi()?.getLatestApk(App.instance.packageName)
        response?.compose(ThreadSwitchTransformer())?.subscribe(object : CallbackListObserver<BaseBean<MutableList<ApkBean>>?>() {
            override fun onSucceed(bean: BaseBean<MutableList<ApkBean>>?) {
                bean?.let {
                    if (it.isSuccessed()) {
                        val beans = it.getData()
                        beans?.let {
                            if (beans.size > 0) {
                                body(beans[0])
                            } else {
                                body(null)
                            }
                        }
                    }
                }
            }

            override fun onFailed() {
                body(null)
            }
        })
    }

    override fun getLatestFirm(body: (firm: FirmBean?) -> Unit) {
        val response = SLMRetrofit.getInstance().getApi()?.getLatestFirm(Constants.WATCH_TYPE)
        response?.compose(ThreadSwitchTransformer())?.subscribe(object : CallbackListObserver<BaseBean<MutableList<FirmBean>>?>() {
            override fun onSucceed(bean: BaseBean<MutableList<FirmBean>>?) {
                bean?.let {
                    if (it.isSuccessed()) {
                        val beans = it.getData()
                        beans?.let {
                            if (beans.size > 0) {
                                body(beans[0])
                            } else {
                                body(null)
                            }
                        }
                    }
                }
            }

            override fun onFailed() {
                body(null)
            }
        })
    }

    override fun downloadApk(attachmentId: Long, body: (mResponse: Response<ResponseBody>?) -> Unit) {

        SLMRetrofit.getInstance().getApi()?.downloadApk(attachmentId)
                ?.compose(DownloadTransformer())
                ?.subscribe(object : Observer<Response<ResponseBody>> {
                    override fun onNext(response: Response<ResponseBody>) {
                        Log.e("downloadApk", "----------------------------------------------------")
                        body(response)
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable) {

                    }
                })
    }

    override fun downloadDfu(attachmentId: Long, body: (mResponseBody: Response<ResponseBody>?) -> Unit) {
        SLMRetrofit.getInstance().getApi()?.downloadDfu(attachmentId)
                ?.compose(DownloadTransformer())
                ?.subscribe(object : Observer<Response<ResponseBody>> {
                    override fun onNext(response: Response<ResponseBody>) {
                        body(response)
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable) {

                    }
                })
    }

    /**
     *
     */
    override fun dowloadFile(url: String, path: String, listener: DownloadCallback) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        RxNet().download(App.instance.getToken(), url, path, listener)
    }

    /**
     * 安装apk,私有目录
     * @param mContext
     * @param filePath
     */
    override fun installApk(mContext: Activity, filePath: String?, complete :()-> Unit) {
        val apkFile = File(filePath)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.e(UpdateModel::class.java.simpleName, "版本大于 N ，开始使用 fileProvider 进行安装")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val contentUri = FileProvider.getUriForFile(mContext
                    , "com.lhzw.bluetooth.fileprovider", apkFile)
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
        }
        mContext.startActivityForResult(intent, 0x6666)
    }

    /**
     * 跳转到设置-允许安装未知来源-页面
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun startInstallPermissionSettingActivity(mContext: Activity) {
        //后面跟上包名，可以直接跳转到对应APP的未知来源权限设置界面。使用startActivityForResult 是为了在关闭设置界面之后，获取用户的操作结果，然后根据结果做其他处理
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + mContext.packageName))
        mContext.startActivityForResult(intent, 0x5555)
    }
}