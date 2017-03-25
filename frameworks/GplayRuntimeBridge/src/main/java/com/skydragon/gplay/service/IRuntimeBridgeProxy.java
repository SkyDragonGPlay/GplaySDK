package com.skydragon.gplay.service;

import java.util.Map;

/**
 * Created by zhangjunfei on 16/3/16.
 */
public interface IRuntimeBridgeProxy {
    // 同步方法调用
    Object invokeMethodSync(String method, Map<String, Object> args);
    // 异步方法调用
    void invokeMethodAsync(String method, Map<String, Object> args, IRuntimeCallback callback);
}
