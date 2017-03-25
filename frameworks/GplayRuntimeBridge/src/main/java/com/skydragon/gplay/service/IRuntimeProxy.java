package com.skydragon.gplay.service;

public interface IRuntimeProxy {
    /* 下载游戏相关 */
    void onDownloadGameStart();
    void onDownloadGameProgress(int downloadedPercent, int downloadSpeed);
    void onDownloadGameSuccess(String jsonStr);
    void onDownloadGameFailure(String jsonStr);
    
    /**
     * 消息接收: 下载引擎,下载游戏等消息
     * @param jsonMsg
     */
    void onMessage(String jsonMsg);

    /* 进入游戏 */
    void onGameEnter();
    
    /* 游戏退出 */
    void onGameExit();
    
}
