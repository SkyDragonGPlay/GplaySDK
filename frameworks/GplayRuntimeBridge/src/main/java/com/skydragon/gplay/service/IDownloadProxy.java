package com.skydragon.gplay.service;

public interface IDownloadProxy {
    void onDownloadStart();
    void onDownloadSuccess();
    void onDownloadFailure(String error);
}
