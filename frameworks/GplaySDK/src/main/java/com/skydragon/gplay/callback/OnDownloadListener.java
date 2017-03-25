package com.skydragon.gplay.callback;

/**
 * 下载回调
 */
public interface OnDownloadListener {
    void onStart();

    void onProgress(long downloadedSize, long totalSize);

    void onSuccess();

    void onFailure(String msg);

    void onCancel();
}
