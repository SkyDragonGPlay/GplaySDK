package com.skydragon.gplay.callback;

import com.skydragon.gplay.IGplayService;

/**
 * SDK下载回调
 */
public interface OnPrepareRuntimeListener {
    void onPrepareRuntimeStart();

    void onPrepareRuntimeProgress(long downloadedSize, long totalSize);

    void onPrepareRuntimeSuccess(IGplayService service);

    void onPrepareRuntimeFailure(String msg);

    void onPrepareRuntimeCancel();
}
