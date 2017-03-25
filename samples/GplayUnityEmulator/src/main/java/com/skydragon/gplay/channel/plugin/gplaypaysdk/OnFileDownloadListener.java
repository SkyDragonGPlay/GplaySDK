package com.skydragon.gplay.channel.plugin.gplaypaysdk;

/**
 * Created by zhangjunfei on 16/3/5.
 */
public interface OnFileDownloadListener {
    void onFailure();
    void onSuccess();
    void onStart();
    void onProgress(long downloaded, long total);
}

