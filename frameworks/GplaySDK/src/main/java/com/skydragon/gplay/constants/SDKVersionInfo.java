package com.skydragon.gplay.constants;

public class SDKVersionInfo {
    public String mSDKVersionStr;
    public int mSDKVersion;
    public String mSDKDownloadUrl;
    public String md5;

    @Override
    public String toString() {
        return "{SDKVersionStr:" + mSDKVersionStr +
                ",SDKVersion:" + mSDKVersion +
                ",SDKDownloadUrl:" + mSDKDownloadUrl +
                ",md5:" + md5 + "}";
    }
}
