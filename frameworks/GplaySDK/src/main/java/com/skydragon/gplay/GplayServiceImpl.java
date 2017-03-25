package com.skydragon.gplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.skydragon.gplay.callback.ICallback;
import com.skydragon.gplay.callback.OnPrepareRuntimeListener;
import com.skydragon.gplay.service.IDownloadProxy;
import com.skydragon.gplay.service.IRuntimeBridge;
import com.skydragon.gplay.service.IRuntimeCallback;
import com.skydragon.gplay.service.IRuntimeProxy;
import com.skydragon.gplay.thirdsdk.IChannelSDKBridge;
import com.skydragon.gplay.thirdsdk.IChannelSDKServicePlugin;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class GplayServiceImpl implements IGplayService {

    private RuntimeBridgeHelper mBridgeHelper;

    private IChannelSDKBridge mChannelSDKBridge;

    private IChannelSDKServicePlugin mChannelSDKServicePlugin;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public GplayServiceImpl(IRuntimeBridge runtimeBridge) {
        mBridgeHelper = RuntimeBridgeHelper.getInstance();
        mBridgeHelper.setRuntimeBridge(runtimeBridge);
    }

    void init(Context context, String channelId, String runtimeDir, String cacheDir, IChannelSDKBridge channelSDKBridge, IChannelSDKServicePlugin channelSDKServicePlugin, OnPrepareRuntimeListener listener) {
        mBridgeHelper.init(context, channelId, runtimeDir, cacheDir);
        mChannelSDKBridge = channelSDKBridge;
        this.mChannelSDKServicePlugin = channelSDKServicePlugin;
        listener.onPrepareRuntimeSuccess(this);
    }

    /**
     * 启动游戏
     *
     * @param activity .
     * @param appId    游戏标识，Gplay 给渠道分配的ID
     * @param proxy    .
     */
    public void startGame(final Activity activity, final String appId, final IGplayServiceProxy proxy) {
        startGame(activity, appId, false, proxy);
    }

    @Override
    public void startGame(final Activity activity, final String appId, final boolean bHostManagerRuntime, final IGplayServiceProxy proxy) {
        mBridgeHelper.setRuntimeProxy(new IRuntimeProxy() {
            @Override
            public void onDownloadGameStart() {
                //下载游戏相关文件开始时设置runtime 服务器地址,如果不设置则使用默认地址
                setRuntimeHostUrl(Gplay.getRuntimeServerUrl());
                proxy.onDownloadGameStart();
            }

            @Override
            public void onDownloadGameProgress(int downloadedPercent, int downloadSpeed) {
                proxy.onDownloadGameProgress(downloadedPercent, downloadSpeed);
            }

            @Override
            public void onDownloadGameSuccess(String jsonStr) {
                mBridgeHelper.initRuntime(activity, mChannelSDKBridge, mChannelSDKServicePlugin);
                //runtime运行环境初始化好之后设置统一sdk服务器地址,如果不设置则使用默认地址
                setUnitSDKHostUrl(Gplay.getUnitSDKServerUrl());
                proxy.onDownloadGameSuccess();
            }

            @Override
            public void onDownloadGameFailure(String error) {
                proxy.onDownloadGameFailure(error);
            }

            @Override
            public void onMessage(String jsonMsg) {
                proxy.onMessage(jsonMsg);
            }

            @Override
            public void onGameEnter() {
                View view = getGameView();
                View modifyView = proxy.onBeforeSetContentView(view);
                proxy.onSetContentView(modifyView);
                proxy.onGameStart();
            }

            @Override
            public void onGameExit() {
                proxy.onGameExit();
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBridgeHelper.startGame(activity, appId, bHostManagerRuntime);
            }
        });
    }

    @Override
    public void startGame(final Activity activity, final JSONObject jsonGameInfo, final IGplayServiceProxy proxy) {
        mBridgeHelper.setRuntimeProxy(new IRuntimeProxy() {
            @Override
            public void onDownloadGameStart() {
                proxy.onDownloadGameStart();
            }

            @Override
            public void onDownloadGameProgress(int downloadedPercent, int downloadSpeed) {
                proxy.onDownloadGameProgress(downloadedPercent, downloadSpeed);
            }

            @Override
            public void onDownloadGameSuccess(String jsonStr) {
                //runtime运行环境初始化好之后设置统一sdk服务器地址,如果不设置则使用默认地址
                mBridgeHelper.initRuntime(activity, mChannelSDKBridge, mChannelSDKServicePlugin);
                setUnitSDKHostUrl(Gplay.getUnitSDKServerUrl());
                proxy.onDownloadGameSuccess();
            }

            @Override
            public void onDownloadGameFailure(String error) {
                proxy.onDownloadGameFailure(error);
            }

            @Override
            public void onMessage(String jsonMsg) {
                proxy.onMessage(jsonMsg);
            }

            @Override
            public void onGameEnter() {
                View view = getGameView();
                View modifyView = proxy.onBeforeSetContentView(view);
                proxy.onSetContentView(modifyView);
                proxy.onGameStart();
            }

            @Override
            public void onGameExit() {
                proxy.onGameExit();
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBridgeHelper.startGame(activity, jsonGameInfo);
            }
        });
    }

    @Override
    public void startGameForDebug(final Activity activity, final JSONObject jsonGameInfo, final IGplayServiceProxy proxy) {
        mBridgeHelper.setRuntimeProxy(new IRuntimeProxy() {
            @Override
            public void onDownloadGameStart() {
                proxy.onDownloadGameStart();
            }

            @Override
            public void onDownloadGameProgress(int downloadedPercent, int downloadSpeed) {
                proxy.onDownloadGameProgress(downloadedPercent, downloadSpeed);
            }

            @Override
            public void onDownloadGameSuccess(String jsonStr) {
                //runtime运行环境初始化好之后设置统一sdk服务器地址,如果不设置则使用默认地址
                mBridgeHelper.initRuntime(activity, mChannelSDKBridge, mChannelSDKServicePlugin);
                setUnitSDKHostUrl(Gplay.getUnitSDKServerUrl());
                proxy.onDownloadGameSuccess();
            }

            @Override
            public void onDownloadGameFailure(String error) {
                proxy.onDownloadGameFailure(error);
            }

            @Override
            public void onMessage(String jsonMsg) {
                proxy.onMessage(jsonMsg);
            }

            @Override
            public void onGameEnter() {
                View view = getGameView();
                View modifyView = proxy.onBeforeSetContentView(view);
                proxy.onSetContentView(modifyView);
                proxy.onGameStart();
            }

            @Override
            public void onGameExit() {
                proxy.onGameExit();
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("GplayServiceImpl","startGameForDebug jsonGameInfo=" + jsonGameInfo.toString());
                mBridgeHelper.startGameForDebug(activity, jsonGameInfo);
            }
        });
    }

    /**
     * 关闭游戏
     */
    public void closeGame() {
        mBridgeHelper.closeGame();
    }

    @Override
    public void setRuntimeHostUrl(String host) {
        mBridgeHelper.setRuntimeHostUrl(host);
    }

    @Override
    public void startSilentDownload() {
        mBridgeHelper.startSilentDownload();
    }

    @Override
    public void stopSilentDownload() {
        mBridgeHelper.stopSilentDownload();
    }

    @Override
    public void setUnitSDKHostUrl(String host) {
        mBridgeHelper.setUnitSDKHostUrl(host);
    }

    /**
     * 生命周期onPause
     */
    public void onPause() {
        mBridgeHelper.onPause();
    }

    /**
     * 生命周期onResume
     */
    public void onResume() {
        mBridgeHelper.onResume();
    }

    /**
     * 生命周期onStop
     */
    public void onStop() {
        mBridgeHelper.onStop();
    }

    /**
     * 生命周期onDestroy
     */
    public void onDestroy() {
        mBridgeHelper.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        mBridgeHelper.onWindowFocusChanged(hasFocus);
    }

    /**
     * 生命周期onNewIntent
     *
     */
    public void onNewIntent(Intent intent) {
        mBridgeHelper.onNewIntent(intent);
    }

    /**
     * 生命周期onActivityResult
     *
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mBridgeHelper.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 清除所有游戏缓存
     *
     * @return
     */
    public boolean cleanAllGameCache() {
        return mBridgeHelper.cleanAllGameCache();
    }

    /**
     * 清除游戏缓存
     *
     * @param tag 游戏gameKey
     * @return
     */
    public boolean cleanGameCache(String tag) {
        return mBridgeHelper.cleanGameCache(tag);
    }

    /**
     * 取消游戏下载
     */
    public void cancelStartGame() {
        mBridgeHelper.cancelStartGame();
    }

    @Override
    public void retryStartGame(String gameKey) {
        mBridgeHelper.retryStartGame(gameKey);
    }

    /**
     * 静默下载游戏开关,需要在startGame之前调用
     *
     */
    public void setSilentDownloadEnabled(boolean enabled) {
        mBridgeHelper.setSilentDownloadEnabled(enabled);
    }

    public void invokeMethodAsync(final String method, Map args, final ICallback callback) {
        if(method == null || method.equals("")) {
            if(callback != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("result_code", -1);
                result.put("result_msg", "Invoke method failed, please set a correct method !");
                callback.onCallback(method, result);
            }
            return;
        }

        mBridgeHelper.invokeMethodAsync(method, args, new IRuntimeCallback() {
            @Override
            public Object onCallback(String from, Map<String, Object> args) {
                if(callback != null){
                    args.put("result_msg", "Invoke method success!");
                    return callback.onCallback(from, args);
                }
                return null;
            }
        });
    }

    @Override
    public Object invokeMethodSync(String method, Map args) {
        if(method == null || method.equals(""))
            return null;
        return mBridgeHelper.invokeMethodSync(method, args);
    }

    @Override
    public JSONObject getGameInfo() {
        return mBridgeHelper.getGameInfo();
    }

    void cancelPrepareRuntime() {
        mBridgeHelper.cancelPrepareRuntime();
    }

    void prepareRuntime(Context context) {
        mBridgeHelper.prepareRuntime(context);
    }

    void setPrepareRuntimeProxy(IDownloadProxy proxy) {
        mBridgeHelper.setPrepareRuntimeProxy(proxy);
    }

    /**
     * 得到用于显示的view
     *
     */
    private View getGameView() {
        return mBridgeHelper.getGameView();
    }
}
