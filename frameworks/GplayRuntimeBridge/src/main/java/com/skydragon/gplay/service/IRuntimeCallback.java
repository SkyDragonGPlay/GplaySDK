package com.skydragon.gplay.service;

import java.util.Map;

/**
 * Created by zhangjunfei on 16/3/16.
 */
public interface IRuntimeCallback {
    // 异步方法回调接口
    Object onCallback(String from, Map<String, Object> args);
}
