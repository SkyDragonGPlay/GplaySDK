package com.skydragon.gplay.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.skydragon.gplay.Gplay;
import com.skydragon.gplay.callback.OnDownloadListener;
import com.skydragon.gplay.constants.FileConstants;
import com.skydragon.gplay.constants.SDKVersionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public final class RuntimeSDKDownloader {
    
    private static final String TAG = "RuntimeSDKDownloader";

    private static final String REQUEST_SDK_VERSION_URL = Gplay.getServerUrl() + "api/sdk/version";

    private static final int MSG_DOWNLOAD_START = 0;
    private static final int MSG_DOWNLOAD_PROGRESS = 1;
    private static final int MSG_DOWNLOAD_SUCCESS = 2;
    private static final int MSG_DOWNLOAD_FAILED = 3;
    private static final int MSG_DOWNLOAD_CANCEL = 4;

    private static final int CURRENT_STATE_INIT = 1;
    private static final int CURRENT_STATE_REQUEST_REAL_SDK_INFO = 2;
    private static final int CURRENT_STATE_PREPARE_TO_DOWNLOAD = 4;
    private static final int CURRENT_STATE_FETCH_REAL_SDK = 5;

    private static SDKVersionInfo sSDKVersionInfo = null;

    private boolean isCanceled;
    private boolean mHasSentStart = false;

    private String mChannelID;

    private OnDownloadListener sOnPreparePluginsListener; // TODO rename

    private void setOnPreparePluginsListener(OnDownloadListener lis) {
        sOnPreparePluginsListener = lis;
    }

    // 处理下载状态变化消息，通知OnGplaySDKDownloadListener
    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case RuntimeSDKDownloader.MSG_DOWNLOAD_START:
                        if (sOnPreparePluginsListener != null) {
                            sOnPreparePluginsListener.onStart();
                        }
                        break;
                    case RuntimeSDKDownloader.MSG_DOWNLOAD_PROGRESS:
                        if (sOnPreparePluginsListener != null) {
                            sOnPreparePluginsListener.onProgress(msg.arg1, msg.arg2);
                        }
                        break;
                    case RuntimeSDKDownloader.MSG_DOWNLOAD_SUCCESS:
                        if (sOnPreparePluginsListener != null) {
                            sOnPreparePluginsListener.onSuccess();
                        }
                        break;
                    case RuntimeSDKDownloader.MSG_DOWNLOAD_FAILED:
                        if (sOnPreparePluginsListener != null) {
                            String errorMsg = (String) msg.obj;
                            sOnPreparePluginsListener.onFailure(errorMsg);
                        }
                        break;
                    case RuntimeSDKDownloader.MSG_DOWNLOAD_CANCEL:
                        if (sOnPreparePluginsListener != null) {
                            sOnPreparePluginsListener.onCancel();
                        }
                        break;
                }
            }
        }
    };

    public RuntimeSDKDownloader(String channelID) {
        mChannelID = channelID;
    }

    public void start(OnDownloadListener lis) {
        setOnPreparePluginsListener(lis);
        isCanceled = false;
        Log.d(TAG, "RuntimeSDKDownloader start...");
        notifyCurrentStepFinished(CURRENT_STATE_INIT);
    }

    private void notifyCurrentStepFinished(int curState) {
        if (isCanceled) {
            return;
        }
        switch (curState) {
            case CURRENT_STATE_INIT:
                requestRealSDKInfo();
                break;
            case CURRENT_STATE_REQUEST_REAL_SDK_INFO:
                prepareToDownload();
                break;
            case CURRENT_STATE_PREPARE_TO_DOWNLOAD:
                fetchRuntimeSDK();
                break;
            case CURRENT_STATE_FETCH_REAL_SDK:
                onPrepareSuccess();
                break;
            default:
                Log.e(TAG, "Unknown state");
                break;
        }
    }

    private void requestRealSDKInfo() {
        String url = REQUEST_SDK_VERSION_URL + "?chn=" + mChannelID;
        Log.v(TAG, "requestRealSDKInfo url=" + url);
        HttpUtils.requestData(url, new HttpUtils.HttpResponseListener() {
            @Override
            public void onFailure() {
                onPrepareFailed();
            }

            @Override
            public void onSuccess(String response) {
                sSDKVersionInfo = parseSDKInfo(response);
                if (sSDKVersionInfo != null) {
                    notifyCurrentStepFinished(CURRENT_STATE_REQUEST_REAL_SDK_INFO);
                } else {
                    onFailure();
                }
            }
        });
    }

    private void prepareToDownload() {
        notifyCurrentStepFinished(CURRENT_STATE_PREPARE_TO_DOWNLOAD);
    }

    private void fetchRuntimeSDK() {
        final String sdkPath = FileConstants.getGplaySDKPath(sSDKVersionInfo.mSDKVersionStr);
        if (new File(sdkPath).exists()) {
            // 已经是最新版本了，不用再下载
            notifyCurrentStepFinished(CURRENT_STATE_FETCH_REAL_SDK);
            return;
        }

        final String savePath = FileConstants.getGplaySDKPathTemp(sSDKVersionInfo.mSDKVersionStr);
        HttpUtils.downloadFile(sSDKVersionInfo.mSDKDownloadUrl, savePath, new HttpUtils.OnFileDownloadListener() {
            @Override
            public void onStart() {
                onPrepareStart();
            }

            @Override
            public void onFailure() {
                onPrepareFailed();
            }

            @Override
            public void onProgress(long downloaded, long total) {
                onPrepareProgress(downloaded, total);
            }

            @Override
            public void onSuccess() {
                FileUtils.renameFile(savePath, sdkPath);
                notifyCurrentStepFinished(CURRENT_STATE_FETCH_REAL_SDK);
            }
        });
    }

    private static SDKVersionInfo parseSDKInfo(String result) {
        SDKVersionInfo info = null;
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject jsonResult = jsonObject.optJSONObject("result");
            if(null == jsonResult) {
                Log.e(TAG, "invalid response data, not include 'result'");
                return null;
            }
            String status = jsonResult.optString("status");
            if(null == status || !status.equals("ok")) {
                Log.e(TAG, "request sdk info failed, error: " + jsonResult.optString("error"));
                return null;
            }
            info = new SDKVersionInfo();

            JSONObject jsonInfo = jsonObject.optJSONObject("data");
            info.mSDKVersionStr = jsonInfo.optString("version_name");
            info.mSDKVersion = jsonInfo.optInt("version_code");
            info.mSDKDownloadUrl = jsonInfo.optString("url");
            info.md5 = jsonInfo.optString("md5");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info;
    }

    public void onPrepareStart() {
        isCanceled = false;
        obtainStartMessage();
    }
    
    public void onPrepareProgress(long downloaded, long total) {
        obtainProgressMessage(downloaded, total);
    }
    
    public void onPrepareSuccess() {
        // to load jar
        obtainSuccessMessage();
    }
    
    public void onPrepareFailed() {
        onPrepareFailed("download_failed");
    }
    
    public void onPrepareFailed(String msg) {
        Log.e(TAG, "onPrepareFailed : " + msg);
        obtainFailedMessage(msg);
    }
    
    public void onPrepareCancel() {
        Log.i(TAG, "onPrepareCancel");
        isCanceled = true;
        obtainCancelMessage();
    }
    
    private void obtainStartMessage() {
        if(handler != null && !mHasSentStart)
        {
            Message msg = new Message();
            msg.what = MSG_DOWNLOAD_START;
            handler.sendMessage(msg);
            mHasSentStart = true;
        }        
    }

    private void obtainFailedMessage(String errorMsg) {
        if (isCanceled) {
            isCanceled = false;
            return;
        }
        if(handler != null)
        {
            Message msg = new Message();
            msg.what = MSG_DOWNLOAD_FAILED;
            msg.obj = errorMsg;
            handler.sendMessage(msg);
        }
    }

    private void obtainSuccessMessage() {
        if (isCanceled) {
            return;
        }
        FileUtils.removeUnusedSDK(FileConstants.getSDKDir());
        if(handler != null)
        {
            Message msg = new Message();
            msg.what = MSG_DOWNLOAD_SUCCESS;
            handler.sendMessage(msg);
        }
    }

    private void obtainCancelMessage() {
        if(handler != null)
        {
            Message msg = new Message();
            msg.what = MSG_DOWNLOAD_CANCEL;
            handler.sendMessage(msg);
        }
    }

    private void obtainProgressMessage(long downloaded, long total) {
        if(handler != null)
        {
            Message msg = new Message();
            msg.what = MSG_DOWNLOAD_PROGRESS;
            msg.arg1 = (int) downloaded;
            msg.arg2 = (int) total;
            handler.sendMessage(msg);
        }
    }

    public void cancelDownload() {
        Log.i(TAG, "cancelDownload");
        isCanceled = true;
        HttpUtils.cancelDownload();
        onPrepareCancel();
    }
}
