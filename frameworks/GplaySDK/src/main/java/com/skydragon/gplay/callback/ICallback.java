package com.skydragon.gplay.callback;

import java.util.Map;

public interface ICallback {
    // 异步方法回调接口
    Object onCallback(String from, Map<String, Object> args);
}
