package com.lhzw.bluetooth.net.rxnet.callback;

import java.io.File;

import io.reactivex.disposables.Disposable;

/**
 * Date： 2020/6/2 0002
 * Time： 10:06
 * Created by xtqb.
 */
public interface DownloadCallback {
    void onStart(Disposable d);

    void onProgress(long totalByte, long currentByte, int progress);

    void onFinish(File file);

    void onError(String msg);
}
