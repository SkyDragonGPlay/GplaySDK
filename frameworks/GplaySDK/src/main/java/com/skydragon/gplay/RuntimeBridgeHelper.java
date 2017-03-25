package com.skydragon.gplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.skydragon.gplay.service.IDownloadProxy;
import com.skydragon.gplay.service.IRuntimeBridge;
import com.skydragon.gplay.service.IRuntimeCallback;
import com.skydragon.gplay.service.IRuntimeProxy;
import com.skydragon.gplay.thirdsdk.IChannelSDKBridge;
import com.skydragon.gplay.thirdsdk.IChannelSDKServicePlugin;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public final class RuntimeBridgeHelper {

    private static RuntimeBridgeHelper sInstance;

    private IRuntimeBridge mRuntimeBridge;

    private RuntimeBridgeHelper() { }

    public static RuntimeBridgeHelper getInstance() {
        if(null == sInstance) {
            sInstance = new RuntimeBridgeHelper();
        }
        return sInstance;
    }

    public void setRuntimeBridge(IRuntimeBridge bridge) {
        mRuntimeBridge = bridge;
    }


    public boolean init(Context context, String cappid, String runtimeDir, String rootPath) {
        Map<String,Object> params = new HashMap<>();
        params.put("context", context);
        params.put("channelID", cappid);
        params.put("runtimeDir", runtimeDir);
        params.put("cacheDir", rootPath);
        return (Boolean)mRuntimeBridge.invokeMethodSync("init", params);
    }

    public void initRuntime(Activity activity, IChannelSDKBridge channelSDKBridge, IChannelSDKServicePlugin channelSDKServicePlugin) {
        //Runtime加载好之后设置api接口服务器访问地址
        Map<String,Object> params = new HashMap<>();
        params.put("activity", activity);
        params.put("channelSDKBridge", channelSDKBridge);
        params.put("channelSDKServicePlugin", channelSDKServicePlugin);
        mRuntimeBridge.invokeMethodSync("initRuntime", params);
    }

    public View getGameView() {
        Map<String,Object> params = new HashMap<>();
        return (View)mRuntimeBridge.invokeMethodSync("getGameView", params);
    }

    public void closeGame() {
        Map<String,Object> params = new HashMap<>();
        mRuntimeBridge.invokeMethodSync("closeGame", params);
    }

    public void onNewIntent(Intent intent) {
        Map<String,Object> params = new HashMap<>();
        params.put("intent", intent);
        mRuntimeBridge.invokeMethodSync("onNewIntent", params);
    }

    public void onPause() {
        Map<String,Object> params = new HashMap<>();
        mRuntimeBridge.invokeMethodSync("onPause", params);
    }

    public void onResume() {
        Map<String,Object> params = new HashMap<>();
        mRuntimeBridge.invokeMethodSync("onResume", params);
    }

    public void onDestroy() {
        Map<String,Object> params = new HashMap<>();
        mRuntimeBridge.invokeMethodSync("onDestroy", params);
    }

    public void onStop() {
        Map<String,Object> params = new HashMap<>();
        mRuntimeBridge.invokeMethodSync("onStop", params);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        Map<String,Object> params = new HashMap<>();
        params.put("hasFocus", hasFocus);
        mRuntimeBridge.invokeMethodSync("onWindowFocusChanged", params);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Map<String,Object> params = new HashMap<>();
        params.put("requestCode", requestCode);
        params.put("resultCode", resultCode);
        params.put("data", data);
        mRuntimeBridge.invokeMethodSync("onActivityResult", params);
    }

    public boolean cleanAllGameCache() {
        Map<String,Object> params = new HashMap<>();
        return (Boolean)mRuntimeBridge.invokeMethodSync("cleanAllGameCache", params);
    }

    public boolean cleanGameCache(String tag) {
        Map<String,Object> params = new HashMap<>();
        params.put("tag", tag);
        return (Boolean)mRuntimeBridge.invokeMethodSync("cleanGameCache", params);
    }

    public String getCacheDir() {
        Map<String,Object> params = new HashMap<>();
        return (String)mRuntimeBridge.invokeMethodSync("getCacheDir", params);
    }

    public void startGame(Activity activity, String gameKey) {
        Map<String,Object> params = new HashMap<>();
        params.put("activity", activity);
        params.put("gameKey", gameKey);
        mRuntimeBridge.invokeMethodSync("startGame", params);
    }

    public void startGame(Activity activity, String gameKey, boolean bHostManagerRuntime) {
        Map<String,Object> params = new HashMap<>();
        params.put("activity", activity);
        params.put("gameKey", gameKey);
        params.put("bHostManagerRuntime", bHostManagerRuntime);
        mRuntimeBridge.invokeMethodSync("startGame", params);
    }

    public void startGame(Activity activity, JSONObject jsonObject ) {
        Map<String,Object> params = new HashMap<>();
        params.put("activity", activity);
        params.put("jsonObject", jsonObject);
        mRuntimeBridge.invokeMethodSync("startGame", params);
    }

    public void startGameForDebug(Activity activity, JSONObject jsonObject) {
        Map<String,Object> params = new HashMap<>();
        params.put("activity", activity);
        params.put("jsonObject", jsonObject);
        mRuntimeBridge.invokeMethodSync("startGameForDebug", params);
    }

    public void retryStartGame(String gameKey) {
        Map<String,Object> params = new HashMap<>();
        params.put("gameKey", gameKey);
        mRuntimeBridge.invokeMethodSync("retryStartGame", params);
    }

    public void cancelStartGame() {
        Map<String,Object> params = new HashMap<>();
        mRuntimeBridge.invokeMethodSync("cancelStartGame", params);
    }

    public void cancelPrepareRuntime() {
        Map<String,Object> params = new HashMap<>();
        mRuntimeBridge.invokeMethodSync("cancelPrepareRuntime", params);
    }

    public void prepareRuntime(Context context) {
        setRuntimeHostUrl(Gplay.getRuntimeServerUrl());

        Map<String,Object> params = new HashMap<>();
        params.put("context", context);
        mRuntimeBridge.invokeMethodSync("prepareRuntime", params);
    }

    public void startGameForLocalDebug(Activity activity, String jsonStrGame) {
        Map<String,Object> params = new HashMap<>();
        params.put("activity", activity);
        params.put("jsonStrGame", jsonStrGame);
        mRuntimeBridge.invokeMethodSync("startGameForLocalDebug", params);
    }

    public void setRuntimeHostUrl(String host) {
        Map<String,Object> params = new HashMap<>();
        params.put("host", host);
        mRuntimeBridge.invokeMethodSync("setRuntimeHostUrl", params);
    }

    public void setUnitSDKHostUrl(String host) {
        Map<String,Object> params = new HashMap<>();
        params.put("host", host);
        mRuntimeBridge.invokeMethodSync("setUnitSDKHostUrl", params);
    }

    public void setRuntimeProxy(IRuntimeProxy proxy) {
        Map<String,Object> params = new HashMap<>();
        params.put("proxy", proxy);
        mRuntimeBridge.invokeMethodSync("setRuntimeProxy", params);
    }

    public void setSilentDownloadEnabled(boolean enabled) {
        Map<String,Object> params = new HashMap<>();
        params.put("enabled", enabled);
        mRuntimeBridge.invokeMethodSync("setSilentDownloadEnabled", params);
    }

    public void startSilentDownload() {
        Map<String,Object> params = new HashMap<>();
        mRuntimeBridge.invokeMethodSync("startSilentDownload", params);
    }

    public void stopSilentDownload() {
        Map<String,Object> params = new HashMap<>();
        mRuntimeBridge.invokeMethodSync("stopSilentDownload", params);
    }

    public void setPrepareRuntimeProxy(IDownloadProxy downloadProxy) {
        Map<String,Object> params = new HashMap<>();
        params.put("downloadProxy", downloadProxy);
        mRuntimeBridge.invokeMethodSync("setPrepareRuntimeProxy", params);
    }

    public JSONObject getGameInfo() {
        Map<String,Object> params = new HashMap<>();
        return (JSONObject)mRuntimeBridge.invokeMethodSync("getGameInfo", params);
    }

    public void invokeMethodAsync(final String method, final Map args, final IRuntimeCallback callback){
        mRuntimeBridge.invokeMethodAsync(method, args, callback);
    }

    public Object invokeMethodSync(String method, Map args){
        return mRuntimeBridge.invokeMethodSync(method, args);
    }
}
