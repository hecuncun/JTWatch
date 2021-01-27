package com.lhzw.bluetooth.net.rxnet;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.lhzw.bluetooth.net.rxnet.callback.DownloadCallback;
import com.lhzw.bluetooth.net.rxnet.callback.DownloadListener;
import com.lhzw.bluetooth.net.rxnet.core.RetrofitFactory;
import com.lhzw.bluetooth.net.rxnet.utils.CommonUtils;
import com.lhzw.bluetooth.net.rxnet.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * 支持断点续传
 * Date： 2020/6/2 0002
 * Time： 10:06
 * Created by xtqb.
 */
public class RxNet {

    public static boolean enableLog = true;

    public void download(String token, final String url, final String filePath, final DownloadCallback callback) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(filePath) || callback == null) {
            if (null != callback) {
                callback.onError("url or path empty");
            }
            return;
        }
//        File oldFile = new File(filePath);
//        if (oldFile.exists()) {
//            if (null != callback) {
//                callback.onFinish(oldFile);
//            }
//            return;
//        }

        DownloadListener listener = responseBody -> saveFile(responseBody, url, filePath, callback);

        new RetrofitFactory().downloadFile(token, url, CommonUtils.getTempFile(url, filePath).length(), listener, new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {
                if (null != callback) {
                    callback.onStart(d);
                }
            }

            @Override
            public void onNext(final ResponseBody responseBody) {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                LogUtils.i("onError " + e.getMessage());
                if (null != callback) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onComplete() {
                LogUtils.i("download onComplete ");
            }
        });

    }

    private void saveFile(final ResponseBody responseBody, String url, final String filePath, final DownloadCallback callback) {
        boolean downloadSuccss = true;
        final File tempFile = CommonUtils.getTempFile(url, filePath);
        try {
            writeFileToDisk(responseBody, tempFile.getAbsolutePath(), callback);
        } catch (Exception e) {
            e.printStackTrace();
            downloadSuccss = false;
        }

        if (downloadSuccss) {
            final boolean renameSuccess = tempFile.renameTo(new File(filePath));
            new Handler(Looper.getMainLooper()).post(() -> {
                if (null != callback && renameSuccess) {
                    callback.onFinish(new File(filePath));
                }
            });
        }
    }

    private void writeFileToDisk(ResponseBody responseBody, String filePath, final DownloadCallback callback) {
        long totalByte = responseBody.contentLength();
        long downloadByte = 0;
        File file = new File(filePath);
//        if(file.delete()){
//            file.delete();
//        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rwd");
            byte[] buffer = new byte[1024];
//            long tempFileLen = file.length();
            randomAccessFile.seek(0);
            while (true) {
                int len = responseBody.byteStream().read(buffer);
                if (len == -1) {
                    break;
                }
                randomAccessFile.write(buffer, 0, len);
                downloadByte += len;
                callbackProgress(totalByte, downloadByte, callback);
                Log.e("UPDATEWATCH", "downloadByte : " + downloadByte);
            }
            randomAccessFile.close();
            randomAccessFile = null;
            callbackProgress(-1, -1, callback);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                LogUtils.e(e.getMessage());
            }

        }


    }

    private void callbackProgress(final long totalByte, final long downloadByte, final DownloadCallback callback) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (null != callback) {
                if (totalByte != -1 && downloadByte != -1) {
                    callback.onProgress(totalByte, downloadByte, (int) ((downloadByte * 100) / totalByte));
                } else {
                    callback.onProgress(-1, -1, -3);
                }
            }
        });
    }

}
