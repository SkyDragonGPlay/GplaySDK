// IPrepareRuntimeListener.aidl
package com.skydragon.gplay.service;

// Declare any non-default types here with import statements

interface IPrepareRuntimeListener {
    void onStart();
    void onSucess();
    void onFailed();
    void onCancel();
}
