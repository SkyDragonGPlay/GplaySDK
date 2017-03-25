package com.skydragon.gplay.demo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.skydragon.gplay.Gplay;
import com.skydragon.gplay.callback.OnDownloadListener;
import com.skydragon.gplay.demo.Constants;
import com.skydragon.gplay.demo.utils.Utils;

import java.io.File;

/**
 * 预下载服务, 开启此服务的目的是避免在下载Runtime相关文件过程中对接入APP的影响
 * 注:此服务必须运行在与接入APP不同的进程中,具体配置请看AndroidManifest.xml文件
 */
public class PrepareRuntimeService extends Service {

    private final static String TAG = "PrepareRuntimeService";

    private PrepareRuntimeServiceImpl mPrepareRuntimeServiceImpl;

    final RemoteCallbackList<IPrepareRuntimeListener> mCallbacks = new RemoteCallbackList <IPrepareRuntimeListener>();

    private IPrepareRuntimeListener mCurrentRuntimeListener;

    private static final int ACTION_START = 1;
    private static final int ACTION_CANCEL = 2;
    private static final int ACTION_PROGRESS = 3;
    private static final int ACTION_FAILED = 4;
    private static final int ACTION_SUCCESS = 5;

    private String mChannelID;
    private String mCacheDir;
    private String mProductMode;

    public class PrepareRuntimeServiceImpl extends IPrepareRuntimeService.Stub {

        private Context mContext;

        private boolean mIsCanceled;
        private int mRetryCount;
        private boolean mIsDownloadRuntime;

        private Handler mHandler = new Handler(Looper.getMainLooper());

        public PrepareRuntimeServiceImpl(Context context) {
            mContext = context;
            mIsDownloadRuntime = false;
            mIsCanceled = false;
            mChannelID = Utils.getSharedPreferences(mContext).getString("channel_id", Constants.DEFAULT_CHANNEL_ID);
            mCacheDir = Utils.getSharedPreferences(mContext).getString("rootpath", Environment.getExternalStorageDirectory() + "/gplay_demo");
            mProductMode = Utils.getSharedPreferences(mContext).getString("product_mode", Gplay.getProductMode());

            //设置运行环境
            Gplay.setProductMode(mProductMode);
        }

        @Override
        public void startDownloadRuntime(IPrepareRuntimeListener listener) throws RemoteException {
            if (mIsDownloadRuntime) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "预下载任务正在执行,请稍后", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            if(null != mCurrentRuntimeListener) {
                unRegisterListener();
            }
            if(null != listener) {
                mCallbacks.register(listener);
            }

            mCurrentRuntimeListener = listener;
            mIsDownloadRuntime = true;
            mIsCanceled = false;
            loadRuntime();
        }

        @Override
        public void closePrepareRuntimeService() throws RemoteException {
            unRegisterListener();
            PrepareRuntimeService.this.stopSelf();
        }

        @Override
        public void stopDownload() {
            mIsCanceled = true;
            mIsDownloadRuntime = false;
            Gplay.cancelPreDownloadRuntimeSDK();
            unRegisterListener();
            PrepareRuntimeService.this.stopSelf();
        }

        /**
         * 下载runtime
         */
        private void loadRuntime() {
            if (mIsCanceled) {
                return;
            }
            Log.d(TAG, "loadRuntime");
            Gplay.preDownloadRuntimeSDK(PrepareRuntimeService.this, mChannelID, getGplayRuntimeDefaultDir(mContext), mCacheDir, new OnDownloadListener() {
                @Override
                public void onCancel() {
                    Log.d(TAG, "loadRuntime onCancel");
                    callback(ACTION_CANCEL);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "loadRuntime Start");
                    callback(ACTION_START);
                }

                @Override
                public void onProgress(long downloadedSize, long totalSize) {

                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "loadRuntime sucess!");
                    mRetryCount = 0;
                    mIsDownloadRuntime = false;
                    callback(ACTION_SUCCESS);
                }

                @Override
                public void onFailure(String msg) {
                    Log.d(TAG, "loadRuntime failure");
                    if (mRetryCount < 1) {
                        loadRuntime();
                        mRetryCount++;
                    } else {
                        mIsDownloadRuntime = false;
                        callback(ACTION_FAILED);
                    }
                }
            });
        }
    }

    private void unRegisterListener() {
        if(null != mCallbacks && null != mCurrentRuntimeListener) {
            mCallbacks.unregister(mCurrentRuntimeListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mPrepareRuntimeServiceImpl == null) {
            mPrepareRuntimeServiceImpl = new PrepareRuntimeServiceImpl(this);
        }

        return mPrepareRuntimeServiceImpl;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mPrepareRuntimeServiceImpl != null) {
            mPrepareRuntimeServiceImpl.stopDownload();
        }
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void callback(int action){
        if(null == mCurrentRuntimeListener) return;
        if(null == mCallbacks) return;
        try {
            mCallbacks.beginBroadcast();
            switch (action) {
                case ACTION_START:
                    mCurrentRuntimeListener.onStart();
                    break;
                case ACTION_PROGRESS:
                    break;
                case ACTION_CANCEL:
                    mCurrentRuntimeListener.onCancel();
                    break;
                case ACTION_FAILED:
                    mCurrentRuntimeListener.onFailed();
                    break;
                case ACTION_SUCCESS:
                    mCurrentRuntimeListener.onSucess();
                    break;
            }
            mCallbacks.finishBroadcast();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getGplayRuntimeDefaultDir(Context context) {
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
}
