package com.skydragon.gplay.service;

import java.util.Map;

public interface IRuntimeBridge {
    // 设置Bridge代理类，用于Client层访问Host层的方法
    void setBridgeProxy(IRuntimeBridgeProxy proxy);

    // 获取Bridge代理类
    IRuntimeBridgeProxy getBridgeProxy();

    // 设置配置键值对
    void setOption(String key, Object value);

    // 获取配置键值对
    Object getOption(String key);

    // 同步方法调用
    Object invokeMethodSync(String method, Map<String, Object> args);

    // 异步方法调用
    void invokeMethodAsync(final String method, final Map<String, Object> args, final IRuntimeCallback callback);

}
