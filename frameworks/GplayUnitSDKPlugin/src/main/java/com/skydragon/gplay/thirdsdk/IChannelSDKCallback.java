package com.skydragon.gplay.thirdsdk;

/**
 * Created by zhangjunfei on 15/12/25.
 */
public interface IChannelSDKCallback {
    // 异步方法回调接口
    void onCallback(int resultCode, String resultJsonMsg);
}
