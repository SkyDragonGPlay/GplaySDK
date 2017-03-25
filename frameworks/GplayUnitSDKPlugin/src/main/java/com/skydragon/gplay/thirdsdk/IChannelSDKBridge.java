package com.skydragon.gplay.thirdsdk;

import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import java.util.Map;

public interface IChannelSDKBridge {

    /**
     * 第一个被调用的方法
     * @param context
     * @param channelId 渠道ID
     * @param jsonStr 游戏信息等数据
     * @param channelSDKServicePlugin 渠道服务插件实现对象
     */
    void init(Context context, String channelId, JSONObject jsonStr, IChannelSDKServicePlugin channelSDKServicePlugin);

    // 同步方法调用

    /**
     *
     * @param method
     * @param jsonStr
     * @return
     */
    String invokeMethodSync(String method, String jsonStr);

    // 异步方法调用
    void invokeMethodAsync(String method, String jsonStr, IChannelSDKCallback callback);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onPause();

    void onResume();

    void onNewIntent(Intent intent);

    void onStop();

    void onDestroy();

    void onRestart();

}
