package com.skydragon.gplay.loader;

import android.annotation.SuppressLint;
import android.util.Log;

import com.skydragon.gplay.constants.FileConstants;
import com.skydragon.gplay.service.IRuntimeBridge;
import com.skydragon.gplay.utils.FileUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

@SuppressLint("NewApi")
public final class DexLoader {

    private static final String TAG = "DexLoader";
    private static final boolean DEBUG = false;
    private static final String RUNTIME_BRIDGE_IMPL_CLASS_NAME = "com.skydragon.gplay.runtime.RuntimeBridge";

    private static DexClassLoader sDexClassLoader = null;

    private static HashMap<String, Object> sClassInstanceMap = new HashMap<>(5);

    private static IRuntimeBridge sRuntimeBridge;

    public static IRuntimeBridge loadRuntimeBridgeImpl() {
        sRuntimeBridge =  (IRuntimeBridge) getClassInstance(RUNTIME_BRIDGE_IMPL_CLASS_NAME);
        return sRuntimeBridge;
    }

    private static Object getClassInstance(String className) {
        if (DEBUG) {
            Class<?> cls;
            try {
                cls = Class.forName(className);
                return cls.newInstance();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Object ret = sClassInstanceMap.get(className);

            if (ret != null) {
                Log.d(TAG, "Return " + className + " in cache!");
                return ret;
            }

            try {
                if (sDexClassLoader == null) {
                    init();
                }

                if (sDexClassLoader == null) {
                    return null;
                }

                Class<?> cls = sDexClassLoader.loadClass(className);
                Constructor<?> ctor = cls.getDeclaredConstructor();
                ctor.setAccessible(true);
                ret = ctor.newInstance();
                sClassInstanceMap.put(className, ret);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ret;
        }
        return null;
    }

    private static void init() {
        String sdkPath = FileConstants.getLatestGplaySDKPath();
        if (!FileUtils.checkFileExist(sdkPath)) {
            Log.e(TAG, "Oops, sdkPath (" + sdkPath + ") doesn't exist!");
            return;
        }

        String optimizedDexDir = FileConstants.getGplayOptPath();
        if (optimizedDexDir == null) {
            Log.e(TAG, "optimizedDexDir is null!");
            return;
        }

        try {
            ClassLoader parent;
            parent = DexLoader.class.getClassLoader();
            sDexClassLoader = new DexClassLoader(sdkPath, optimizedDexDir, null, parent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
