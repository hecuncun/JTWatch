package com.lhzw.bluetooth.net.rxnet.callback;

import okhttp3.ResponseBody;

/**
 * Date： 2020/6/2 0002
 * Time： 10:06
 * Created by xtqb.
 */
public interface DownloadListener {
    void onStart(ResponseBody responseBody);
}
