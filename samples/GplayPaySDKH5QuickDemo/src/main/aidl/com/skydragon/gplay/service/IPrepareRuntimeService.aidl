// IPrepareRuntimeService.aidl
package com.skydragon.gplay.service;
import com.skydragon.gplay.service.IPrepareRuntimeListener;
// Declare any non-default types here with import statements


interface IPrepareRuntimeService {
    void startDownloadRuntime(IPrepareRuntimeListener listener);
    void stopDownload();
    void closePrepareRuntimeService();
}
