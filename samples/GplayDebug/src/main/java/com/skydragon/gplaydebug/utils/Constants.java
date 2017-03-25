package com.skydragon.gplaydebug.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * package : com.skydragon.gplaydebug.utils
 * <p/>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.13 11:59.
 */
public class Constants {
    public static final String KEY_CAHNNEL_ID = "key_channel_id";
    public static final String KEY_GAME_ID = "key_game_id";

    public static final String DEFAULT_CHANNEL_ID = "100000";
    public static final String DEFAULT_GAME_KEY = "qApeEYtPIeqZJILz";
//    public static final String DEFAULT_GAME_KEY = "FsbDWQmSOEwGHDeA";


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
     * 获取默认存放目录
     * @return
     */
    public static String getGplayDebugDefaultDir() {
        return Environment.getExternalStorageDirectory() + "/AGplayDebug";
    }
}