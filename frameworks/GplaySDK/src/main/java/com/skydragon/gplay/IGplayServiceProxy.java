package com.skydragon.gplay;

import android.view.View;

public interface IGplayServiceProxy {
    void onDownloadGameStart();

    void onDownloadGameProgress(int downloadedPercent, int downloadSpeed);

    void onDownloadGameSuccess();

    void onDownloadGameFailure(String error);

    View onBeforeSetContentView(View gameView);

    void onSetContentView(View modifiedView);

    void onMessage(String jsonMsg);

    void onGameStart();

    void onGameExit();
}
