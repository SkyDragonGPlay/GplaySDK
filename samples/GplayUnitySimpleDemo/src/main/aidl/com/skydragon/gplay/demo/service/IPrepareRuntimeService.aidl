// IPrepareRuntimeService.aidl
package com.skydragon.gplay.demo.service;
import com.skydragon.gplay.demo.service.IPrepareRuntimeListener;
// Declare any non-default types here with import statements


interface IPrepareRuntimeService {
    void startDownloadRuntime(IPrepareRuntimeListener listener);
    void stopDownload();
    void closePrepareRuntimeService();
}
