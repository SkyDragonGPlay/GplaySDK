package com.skydragon.gplay.demo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.skydragon.gplay.Gplay;
import com.skydragon.gplay.IGplayService;
import com.skydragon.gplay.callback.ICallback;
import com.skydragon.gplay.callback.OnPrepareRuntimeListener;
import com.skydragon.gplay.channel.plugin.GplayChannelPaySDKPlugin;
import com.skydragon.gplay.channel.plugin.h5.GplayChannelPayH5SDKPlugin;
import com.skydragon.gplay.demo.Constants;
import com.skydragon.gplay.demo.utils.Utils;
import com.skydragon.gplay.thirdsdk.IChannelSDKServicePlugin;

import org.json.JSONException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class PreDownGameService extends Service {
    public static Queue<String> queue = new LinkedList<String>();
    private String TAG = "PreDownGameService";
    private Context mActivity;
    private IGplayService mGPlayService;
    public int isDowning = 0;
    public String preDownGameCId;
    public String runGameCId;
    Handler mHandler = new Handler(Looper.getMainLooper());
    public PreDownGameService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mGPlayService == null && intent.getStringExtra("STOP_PREDOWN") == null) {
            preRuntime();
        } else {
            if (intent.getStringExtra("RUNGAMECID") != null) {
                runGameCId = intent.getStringExtra("RUNGAMECID");
                Log.d(TAG, "runGameCId:" + runGameCId);
            }
            if (intent.getStringExtra("STOP_PREDOWN") != null) {
                stopPreDownGame();
            } else if (intent.getStringExtra("CONTINUE_PREDOWN") != null) {
                showToast(0);
                while (!queue.isEmpty()) {
                    Log.d(TAG, "queue:" + queue);
                    String obj = queue.poll();
                    preDownGameCId = obj;
                    if (preDownGameCId.equals(runGameCId)) {
                        Log.d(TAG, "RunGame and PreDownGame isnot the same one game");
                        showToast(1);
                        continue;
                    } else {
                        try {
                            Map<String, Object> args = getMap(obj);
                            preDownGame(mGPlayService, args);
                            break;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (intent.getStringExtra("GAME_KEY") != null) {
                queue.offer(intent.getStringExtra("GAME_KEY"));
                if (!queue.isEmpty()) {
                    if (isDowning == 0) {
                        String obj = queue.poll();
                        preDownGameCId = obj;
                        try {
                            Map<String, Object> args = getMap(obj);
                            preDownGame(mGPlayService, args);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.d(TAG, "queue is empty");
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        mActivity = this;
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void preRuntime() {
        String mChannelID = Utils.getSharedPreferences(this).getString("channel_id", Constants.DEFAULT_CHANNEL_ID);
        String mCacheDir = Utils.getSharedPreferences(this).getString("rootpath", Environment.getExternalStorageDirectory() + "/gplay_demo");
        int mOperateSdkMode = Integer.parseInt(Utils.getSharedPreferences(this).getString("sdk_choose", "0"));
        IChannelSDKServicePlugin ichannelServicePluginProxy = new GplayChannelPaySDKPlugin();
        if (mOperateSdkMode == Constants.USE_GPLAY_H5_PAYSDK) {
            ichannelServicePluginProxy = new GplayChannelPayH5SDKPlugin();
        }
        Gplay.prepareRuntime(this, mChannelID, mCacheDir, ichannelServicePluginProxy, new OnPrepareRuntimeListener() {
            @Override
            public void onPrepareRuntimeStart() {
                Log.d(TAG, "onPrepareRuntimeStart");
            }

            @Override
            public void onPrepareRuntimeProgress(long downloadedSize, long totalSize) {
                Log.d(TAG, "onPrepareRuntimeProgress");
            }

            @Override
            public void onPrepareRuntimeSuccess(final IGplayService service) {
                Log.d(TAG, "onPrepareRuntimeSuccess");
                mGPlayService = service;
            }

            @Override
            public void onPrepareRuntimeFailure(String msg) {
                Log.d(TAG, "onPrepareRuntimeFailure");
            }

            @Override
            public void onPrepareRuntimeCancel() {
                Log.d(TAG, "onPrepareRuntimeCancel");
            }
        });
    }

    private Map<String, Object> getMap(String object) throws JSONException {

        SharedPreferences mSettings = Utils.getSharedPreferences(mActivity);
        int strategy = Integer.parseInt(mSettings.getString("predowngame_strategy", "2"));
        Log.d(TAG, "predowngame_strategy:" + strategy);
        Map<String, Object> args = new HashMap<>();
        args.put("GAME_KEY", object);
        args.put("PRELOAD_STRATEGY", strategy);
        args.put("RESOURCE_BUNDLE_NAME", null);
        args.put("HOST_MANAGER", false);
        return args;

    }

    private void preDownGame(final IGplayService iGplayService, final Map<String, Object> arg) {
        isDowning = 1;
        iGplayService.invokeMethodAsync("PRELOAD_GAME", arg, new ICallback() {
            @Override
            public Object onCallback(String from, Map<String, Object> args) {
                Log.d(TAG, "preDownGameCallBack:" + from + "," + args);
                Object ok = 2;
                Object error = 3;
                if (args.get("STATUS") == ok) {
                    while (!queue.isEmpty()) {
                        try {
                            String obj = queue.poll();
                            preDownGameCId = obj;
                            if (preDownGameCId.equals(runGameCId)) {
                                Log.d(TAG, "RunGame and PreDownGame isnot the same one game");
                                showToast(1);
                                continue;
                            } else {
                                Map<String, Object> arg1 = getMap(obj);
                                iGplayService.invokeMethodAsync("PRELOAD_GAME", arg1, this);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (queue.isEmpty()) {
                        isDowning = 0;
                    }
                } else if (args.get("STATUS") == error) {
                    preDownGameCId = null;
                    while (!queue.isEmpty()) {
                        try {
                            String obj = queue.poll();
                            preDownGameCId = obj;
                            if (preDownGameCId.equals(runGameCId)) {
                                Log.d(TAG, "RunGame and PreDownGame isnot the same one game");
                                showToast(1);
                                continue;
                            } else {
                                Map<String, Object> arg1 = getMap(obj);
                                iGplayService.invokeMethodAsync("PRELOAD_GAME", arg1, this);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (queue.isEmpty()) {
                        isDowning = 0;
                    }
                }
                return null;
            }
        });
    }

    private void stopPreDownGame() {
        isDowning = 0;
        if (preDownGameCId != null) {
            queue.offer(preDownGameCId);
        }

        if (mGPlayService != null) {
            mGPlayService.invokeMethodAsync("STOP_PRELOAD_GAME", null, new ICallback() {
                @Override
                public Object onCallback(String from, Map<String, Object> args) {
                    Log.d(TAG, " stop predown:" + args);
                    Object a = 1;
                    if (args.get("RESULT") == a) {
                        showToast(2);
                    }
                    return null;
                }
            });
        } else {
            Log.d(TAG, "mGPlayService is null");
        }
    }

    private void showToast(final int type) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case 0:
                        Toast.makeText(mActivity, "继续预下载", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(mActivity, "当前游戏和预下载不能为同一个游戏", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "停止预下载", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
