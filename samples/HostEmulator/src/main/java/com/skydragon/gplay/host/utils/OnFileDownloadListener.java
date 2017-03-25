package com.skydragon.gplay.host.utils;

/**
 * Created by zhangjunfei on 16/3/6.
 */
public interface OnFileDownloadListener {
    void onFailure();
    void onSuccess();
    void onStart();
    void onProgress(long downloaded, long total);
}
