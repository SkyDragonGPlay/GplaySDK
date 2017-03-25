package com.skydragon.gplay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.skydragon.gplay.service.IPrepareRuntimeListener;
import com.skydragon.gplay.service.IPrepareRuntimeService;

/**
 * 强烈建议接入APP在准备好运行环境之后才显示游戏列表,实现思路如下:
 * 1. 启动PrepareRuntimeService服务(此服务可以由接入APP自己来定制)来准备runtime运行环境
 * 2. Runtime环境准备完毕之后通知接入APP主进程
 * 3. 接入APP收到通知之后则显示游戏列表
 * 4. 关闭PrepareRuntimeService服务及关闭服务所在的进程
 */
public abstract  class BaseGameListActivity extends Activity{
    private static final String TAG = "BaseGameListActivity";

    private IPrepareRuntimeService mPrepareRuntimeService;
    private Intent mPrepareServiceIntent = null;

    private boolean mIsUnbindService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrepareServiceIntent = new Intent("com.skydragon.gplay.demo.service.PREPARE_RUNTIME_SERVICE");
        bindPrepareRuntimeService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkUnbindService();
    }

    /**
     * 预下载服务
     */
    private void bindPrepareRuntimeService() {
        mPrepareServiceIntent.setPackage(this.getPackageName());
        mPrepareServiceIntent.putExtra(Constants.KEY_CHANNEL_ID, Constants.DEFAULT_CHANNEL_ID);
        mPrepareServiceIntent.putExtra(Constants.KEY_CACHE_DIR, Constants.DEFAULT_CACHE_DIR);
        mPrepareServiceIntent.putExtra(Constants.KEY_RUNTIME_DIR, Utils.ensurePathEndsWithSlash(this.getApplicationInfo().dataDir) + "gplay");
        mPrepareServiceIntent.putExtra(Constants.KEY_PRODUCT_MODE, Gplay.getProductMode());

        bindService(mPrepareServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPrepareRuntimeService = IPrepareRuntimeService.Stub.asInterface(service);
            mIsUnbindService = false;
            processPrepareRuntime();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void processPrepareRuntime() {
        try {
            mPrepareRuntimeService.startDownloadRuntime(mListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    IPrepareRuntimeListener mListener = new IPrepareRuntimeListener.Stub() {
        @Override
        public void onStart() throws RemoteException {
            Log.d(TAG, "prepare runtime start.");
        }

        @Override
        public void onSucess() throws RemoteException {
            Log.d(TAG, "prepare runtime sucess.");
            onPrepareRuntimeSuccess();
            checkUnbindService();
            mPrepareRuntimeService.closePrepareRuntimeService();
            mPrepareRuntimeService = null;
        }

        @Override
        public void onFailed() throws RemoteException {
            Log.d(TAG, "prepare runtime failed.");
            onPrepareRuntimeFailed();
        }

        @Override
        public void onCancel() throws RemoteException {
            Log.d(TAG, "prepare runtime cancel.");
            onPrepareRuntimeCancel();
        }

        @Override
        public IBinder asBinder() {
            return (IBinder)mListener;
        }
    };

    protected void stopDownload() {
        if(null != mPrepareRuntimeService ) {
            try {
                mPrepareRuntimeService.stopDownload();
                checkUnbindService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkUnbindService() {
        if(!mIsUnbindService)
            unbindService(mServiceConnection);
        mIsUnbindService = true;
    }

    protected abstract void onPrepareRuntimeSuccess();
    protected abstract void onPrepareRuntimeFailed();
    protected abstract void onPrepareRuntimeCancel();
    protected abstract void onChannelChange();
}
