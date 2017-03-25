package com.skydragon.gplay.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;


import com.skydragon.gplay.demo.utils.FileConstants;
import com.skydragon.gplay.demo.utils.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by zhangjunfei on 16/3/10.
 *
 * 在电脑通过adb 发出指令的命令如下:
 * adb shell am broadcast -a com.skydragon.gplay.COMMAND --ei gplay_emulator_command 1
 */
public class GplayEmulator extends BroadcastReceiver {

    private static final String TAG = "GplayEmulator";

    private static final String KEY_GPLAY_EMULATOR_COMMAND = "gplay_emulator_command";

    /**
     * 清除缓存
     */
    private static final int FLAG_CLEAR_CACHES = 1;

    private Context mContext;

    private static Activity mActivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        int flag = intent.getIntExtra(KEY_GPLAY_EMULATOR_COMMAND, -1);
        switch (flag) {
            case FLAG_CLEAR_CACHES:
                if(null != mActivity) {
                    mActivity.finish();
                }
                clearCacheInThread();
                break;
            default:
                break;
        }
    }

    private void clearAllCaches() {
        Log.d(TAG, "GplayEmulator clearAllCaches called!!!");
        String gameDir = FileConstants.getGplayGameDefaultDir();
//        String runtimeDir = FileConstants.getGplayRuntimeDefaultDir(mContext);

        Utils.deleteSubFile(gameDir);
//        Utils.deleteSubFile(runtimeDir);
    }

    public static void setHostActivity(Activity act) {
        mActivity = act;
    }

    private void clearCacheInThread() {
        File fMark = new File(Environment.getExternalStorageDirectory(), "gplay_clear_cache_mark");
        if(fMark.exists()) {
            fMark.delete();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                clearAllCaches();
                markClearCacheFinished();

            }
        }).start();
    }

    private void markClearCacheFinished() {
        File fMark = new File(Environment.getExternalStorageDirectory(), "gplay_clear_cache_mark");
        if(fMark.exists()) return;
        try {
            fMark.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
