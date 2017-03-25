package com.skydragon.gplay.host.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by zhangjunfei on 16/3/11.
 */
public final class FileConstants {

    /**
     * 获取Gplay在宿主中的存放目录
     * @param context
     * @return
     */
    public static String getGplayRuntimeDefaultDir(Context context) {
        String ret = null;
        PackageManager pm = context.getPackageManager();

        try {
            ret = pm.getApplicationInfo(context.getPackageName(), 0).dataDir + "/";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(ret))
            return "";

        if (!ret.endsWith(File.separator)) {
            ret = ret + File.separator;
        }

        return ret + "gplay/";
    }

    /**
     * 获取游戏存放目录
     * @return
     */
    public static String getGplayGameDefaultDir() {
        return Environment.getExternalStorageDirectory() + "/unity_emulator/";
    }

    /**
     * 获取游戏存放目录
     * @return
     */
    public static String getUnityResDir() {
        return Environment.getExternalStorageDirectory() + "/unity_res/";
    }
}
