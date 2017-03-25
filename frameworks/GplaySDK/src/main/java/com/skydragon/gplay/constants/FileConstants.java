package com.skydragon.gplay.constants;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.skydragon.gplay.utils.FileUtils;

import java.io.File;

public final class FileConstants {

    private static String APP_DATA_DIR;

    private static String GPLAY_ROOT_PATH;

    private static String SDK_PATH;

    private static String DEX_PATH;

    public static void init(Context ctx) {
        if (APP_DATA_DIR == null) {
            String dataDir = getDataDir(ctx);
            APP_DATA_DIR = dataDir;
            File gplayRootDir = new File(dataDir + "/gplay/");
            if (!gplayRootDir.exists()) {
                gplayRootDir.mkdirs();
            }
            GPLAY_ROOT_PATH = gplayRootDir.getPath() + File.separator;

            File sdkPath = new File(GPLAY_ROOT_PATH + "runtime");
            if (!sdkPath.exists()) {
                sdkPath.mkdirs();
            }
            SDK_PATH = sdkPath.getPath() + File.separator;

            File optPath = new File(GPLAY_ROOT_PATH + "dex");
            if (!optPath.exists()) {
                optPath.mkdirs();
            }
            DEX_PATH = optPath.getPath() + File.separator;
        }

        Log.d("FileConstants", "init SDK_PATH:" + SDK_PATH + ", DEX_PATH:" + DEX_PATH);
    }

    public static String getLibsDir() {
        return GPLAY_ROOT_PATH;
    }

    public static String getSDKDir() {
        return SDK_PATH;
    }

    public static String getGplaySDKPath(String version) {
        return SDK_PATH + "libruntime-" + version + ".jar";
    }

    public static String getGplaySDKPathTemp(String version) {
        return getGplaySDKPath(version) + ".temp";
    }

    public static String getLatestGplaySDKPath() {
        return FileUtils.getLatestGplaySDKPath(SDK_PATH);
    }

    public static String getEngineJavaLibraryDirectory(String engine, String engineVersion, String version) {
        String sJavaLibPath =  GPLAY_ROOT_PATH + engine + "/" + engineVersion + "/java/" + version + "/";
        File f = new File(sJavaLibPath);
        if(!f.exists()) {
            f.mkdirs();
        }
        return sJavaLibPath;
    }

    public static String getEngineShareLibraryDirectory(String engine, String engineVersion, String arch) {
        String sShareLibPath =  GPLAY_ROOT_PATH + engine + "/" + engineVersion + "/" + arch + "/";
        File f = new File(sShareLibPath);
        if(!f.exists()) {
            f.mkdirs();
        }
        return sShareLibPath;
    }

    public static String getGplayOptPath() {
        return DEX_PATH;
    }

    private static String getDataDir(Context context) {
        String ret = null;
        try {
            PackageManager pm = context.getPackageManager();
            ret = pm.getApplicationInfo(context.getPackageName(), 0).dataDir;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
