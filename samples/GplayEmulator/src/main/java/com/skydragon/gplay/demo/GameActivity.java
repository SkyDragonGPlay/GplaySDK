package com.skydragon.gplay.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.skydragon.gplay.Gplay;
import com.skydragon.gplay.IGplayService;
import com.skydragon.gplay.IGplayServiceProxy;
import com.skydragon.gplay.callback.OnPrepareRuntimeListener;
import com.skydragon.gplay.channel.plugin.h5.GplayChannelPayH5SDKPlugin;
import com.skydragon.gplay.demo.ui.ProgressView;
import com.skydragon.gplay.demo.ui.TipsManager;
import com.skydragon.gplay.demo.utils.FileConstants;
import com.skydragon.gplay.demo.utils.Utils;
import com.skydragon.gplay.emulator.R;
import com.skydragon.gplay.sprite.GplaySpriteButton;
import com.skydragon.gplay.sprite.GplaySpritePoint;
import com.skydragon.gplay.sprite.OnGplayButtonAction;
import com.skydragon.gplay.thirdsdk.IChannelSDKServicePlugin;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 必须通过uri的方式来启动此activity, 数据格式如下:
 * gplay_emulator:/gplay_channel_666666?gameinfo=xxx
 * xxx 对应游戏信息
 *
 * 在电脑通过adb启动模拟器的命令如下:
 * adb shell am start -a com.skydragon.gplay.VIEW -d gplay_emulator://gplay_channel_666666?gameinfo=eyJwYWNrYWdlX25hbWUiOiJjb20uc2t5ZHJhb25nLmdwbGF5LmVuZ2luZS5jb2Nvcy5qcyIsImdhbWVfbmFtZSI6ImVtdWxhdG9yR2FtZSIsInZlcnNpb25fbmFtZSI6IjEuMCIsIm9yaWVudGF0aW9uIjoiMCIsImRvd25sb2FkX3VybCI6Imh0dHA6Ly8xOTIuMTY4LjAuMTgwOjgwODAvbW9vbndhcnJpb3J2MzMwIiwid2ViaG9va3NfbG9naW5vYXV0aG8iOiJodHRwOi8vMTkyLjE2OC4wLjE0MDo4MDgwL21vb253YXJyaW9ydjMzMC9hdXRobG9naW4ifQ==
 */
public class GameActivity extends Activity implements OnPrepareRuntimeListener, IGplayServiceProxy {

    private static final String TAG = "GameActivity";

    private String mChannelID;
    private String mCacheDir;
    private int mOrientation;
    private String mGameInfoJsonStr;
    private boolean mIsGameStarted;

    private FrameLayout mRootLayout = null;

    private ProgressView mProgressView;
    
    private IGplayService mGplayService;

    private TipsManager mTipsManager;

    private Handler mHandler = new Handler();

    /**
     * 是否是静默模式
     */
    private boolean mIsSilentMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mChannelID = com.skydragon.gplay.demo.Constants.DEFAULT_CHANNEL_ID;
        mCacheDir = FileConstants.getGplayGameDefaultDir();
        Utils.ensureFileExist(mCacheDir);
        mIsGameStarted = false;
        Intent intent = getIntent();
        String scheme = intent.getScheme();
        if (!TextUtils.isEmpty(scheme)) {
            // 打开分享链接的处理逻辑
            Uri uri = intent.getData();
            String schema = uri.getScheme();
            if(!schema.equalsIgnoreCase("gplay_emulator")) {
                super.onCreate(savedInstanceState);
                Toast.makeText(this, "请求数据不符合规范,请确认传入的数据符合gplay_emulator:/gplay_channel_666666?gameinfo=xxx格式要求", Toast.LENGTH_LONG);
                this.finish();
                return;
            }
            mGameInfoJsonStr = uri.getQueryParameter(Constants.KEY_GAME_INFO);
            mGameInfoJsonStr = new String(Base64.decode(mGameInfoJsonStr, Base64.DEFAULT));
            if(null == mGameInfoJsonStr) {
                super.onCreate(savedInstanceState);
                Toast.makeText(this, "请传入游戏数据!", Toast.LENGTH_LONG);
                this.finish();
                return;
            }
            try {
                JSONObject jsonGameInfo = new JSONObject(mGameInfoJsonStr);
                mOrientation = jsonGameInfo.optInt("orientation");
                mIsSilentMode = jsonGameInfo.optBoolean("issilent");
                boolean isClearCache = jsonGameInfo.optBoolean("delete_first", false);
                String cpSdkVersionName = jsonGameInfo.optString("cp_sdk_version_name", "");
                Log.i(TAG, "The game info orientation=" + mOrientation + ",mIsSilentMode=" + mIsSilentMode + ",isCearCache=" + isClearCache);
                Log.i(TAG, "The version name of CPSDK is " + cpSdkVersionName);
                if(isClearCache) {
                    Utils.clearAllCaches();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            super.onCreate(savedInstanceState);
            Toast.makeText(this, "请求数据不符合规范,请确认传入的数据符合gplay_emulator:/gplay_channel_666666?gameinfo=xxx格式要求", Toast.LENGTH_LONG);
            this.finish();
            return;
        }

        mRootLayout = new FrameLayout(this);

        Log.e(TAG, "orientation=" + (mOrientation == 0 ? "landscape" : "portrait"));
        setGameScreenOrientation(mOrientation);

        mTipsManager = new TipsManager(GameActivity.this, mGplayService);
        mTipsManager.setClientId("debug");

        // 准备 runtime 环境, 在 onPrepareRuntimeSuccess 回调中保存 GplayServic 实现, 并启动游戏
        prepareRuntime();

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netStatusReceiver, mFilter);

        super.onCreate(savedInstanceState);
        setContentView(mRootLayout);
    }

    private void prepareRuntime() {
        // 显示加载页面, 准备 Runtime 运行环境, 设置渠道 SDK 代理
        mProgressView = new ProgressView(this);
        if (mOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            mProgressView.setBackgroundResource(R.drawable.bg_default_landscape);
        } else {
            mProgressView.setBackgroundResource(R.drawable.bg_default_portrait);
        }
        mRootLayout.addView(mProgressView);

        String runtimeDir = FileConstants.getGplayRuntimeDefaultDir(this);
        IChannelSDKServicePlugin ichannelServicePluginProxy = new GplayChannelPayH5SDKPlugin();

        Gplay.prepareRuntime(this, mChannelID, runtimeDir, mCacheDir, ichannelServicePluginProxy, this);
  }

    private void startGame() {
        // 会先请求游戏信息, 判断是否需要下载引擎jar 包, 补丁
        // 检查 boot 包完整以后, 启动游戏.

        mGplayService.setSilentDownloadEnabled(mIsSilentMode);

        try {
            JSONObject jsonGameInfo = createGameInfoForDebug(mGameInfoJsonStr);
            mGplayService.startGameForDebug(this, jsonGameInfo, this);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareRuntimeStart() {

    }

    /**
     * 下载Runtime SDK
     * @param downloadedSize
     * @param totalSize
     */
    @Override
    public void onPrepareRuntimeProgress(long downloadedSize, long totalSize) {

    }

    @Override
    public void onPrepareRuntimeSuccess(IGplayService service) {
        mGplayService = service;
        mGplayService.stopSilentDownload();
        startGame();
    }

    @Override
    public void onPrepareRuntimeFailure(String msg) {
        Toast.makeText(GameActivity.this, "加载Gplay失败 : " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPrepareRuntimeCancel() {
        Toast.makeText(GameActivity.this, "下载取消", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDownloadGameStart() {
        Log.d(TAG, "OnGameDownloadListener onDownloadStart");
    }

    @Override
    public void onDownloadGameProgress(int downloadedPercent, int downloadSpeed) {
        mProgressView.updateProgress(downloadedPercent, downloadSpeed);
    }

    @Override
    public void onDownloadGameSuccess() {
        Log.d(TAG, "OnGameDownloadListener onDownloadGameSuccess");
    }

    @Override
    public void onDownloadGameFailure(String error) {
        Log.d(TAG, "OnGameDownloadListener onDownloadFailure : " + error);
        mTipsManager.show(error);
    }

    @Override
    public View onBeforeSetContentView(View gameView) {
        return gameView;
    }

    @Override
    public void onSetContentView(View modifiedView) {
        Log.d(TAG, "OnGameDownloadListener onSetContentView");
        mRootLayout.addView(modifiedView, 0);
        mIsGameStarted = true;
    }



    @Override
    public void onGameStart() {
        Log.d(TAG, "OnGameDownloadListener onGameStart");
        // 进入游戏后将 loading 页面删除掉
        mRootLayout.removeView(mProgressView);
        // 可根据自身需求定制添加
        showSuspensionView();
    }

    @Override
    public void onMessage(String jsonMsg) {
        try {
            JSONObject msgObject = new JSONObject(jsonMsg);
            Log.d(TAG, "OnGameDownloadListener onMessage download_type: " + msgObject.optString("download_type"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGameExit() {
        // 游戏退出时回调, 额外简单的同步逻辑也可以在这实现
        // 由于游戏退出后需要杀死进程, 故额外异步逻辑可能无法正常完成
        Log.d(TAG, "OnGameDownloadListener onGameExit");
        GameActivity.this.finish();
    }

    /**
     * 设置游戏场景横竖屏
     */
    private void setGameScreenOrientation(int screenOrientation) {
        // 设置游戏全屏显示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 设置屏幕方向
        setRequestedOrientation(screenOrientation);
    }

    private GplaySpritePoint mSuspensionPoint;

    /**
     * 添加返回按钮
     */
    private void showSuspensionView() {

        Display display = this.getWindowManager().getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        Drawable dWhitePoint = this.getResources().getDrawable(R.drawable.whitepoint);

        int whitePointSize = (int)(48 * this.getResources().getDisplayMetrics().density);
        int distance = (int)(28 * this.getResources().getDisplayMetrics().density);
        GplaySpritePoint whitePoint = new GplaySpritePoint(this, mRootLayout, screenWidth, screenHeight/4, dWhitePoint, whitePointSize, distance);
        mSuspensionPoint = whitePoint;

        int buttonSize = (int)(32 * this.getResources().getDisplayMetrics().density);

        Drawable dShareButton = this.getResources().getDrawable(R.drawable.gplay_share);
        GplaySpriteButton suspensionShareButton = new GplaySpriteButton(dShareButton, "分享", buttonSize);
        suspensionShareButton.setGplayButtonAction(new OnGplayButtonAction() {
            @Override
            public void onInvoke() {
                share();
            }
        });
        suspensionShareButton.init(this, 0, 100, 100);
        whitePoint.addSuspendButton(suspensionShareButton);

        Drawable dShortcutButton = this.getResources().getDrawable(R.drawable.gplay_shortcut);
        GplaySpriteButton suspensionShortcutButton = new GplaySpriteButton(dShortcutButton, "发送到桌面", buttonSize);
        suspensionShortcutButton.setGplayButtonAction(new OnGplayButtonAction() {
            @Override
            public void onInvoke() {
                sendToDesktop();
            }
        });
        suspensionShortcutButton.init(this, 1, 100, 100);
        whitePoint.addSuspendButton(suspensionShortcutButton);

        Drawable dCloseButton = this.getResources().getDrawable(R.drawable.gplay_close);
        GplaySpriteButton suspensionCloseButton = new GplaySpriteButton(dCloseButton, "退出", buttonSize);
        suspensionCloseButton.setGplayButtonAction(new OnGplayButtonAction() {
            @Override
            public void onInvoke() {
                GameActivity.this.finish();
            }
        });
        suspensionCloseButton.init(this, 2, 100, 100);
        whitePoint.addSuspendButton(suspensionCloseButton);

        whitePoint.setExpandBackground(this.getResources().getDrawable(R.drawable.transparent_background));

        whitePoint.start();
    }



    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_BACK:
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (!mIsGameStarted) {
                    mTipsManager.show(TipsManager.MODE_TYPE_LOADING_BACK);
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (mGplayService != null) {
            mGplayService.onNewIntent(intent);
        }
        super.onNewIntent(intent);
        if(null!=mSuspensionPoint)
            mSuspensionPoint.onNewIntent();
        forceFullScreen();
    }

    @Override
    protected void onPause() {
        GplayEmulator.setHostActivity(null);
        if (mGplayService != null) {
            mGplayService.onPause();
        }
        super.onPause();
        if(null!=mSuspensionPoint)
            mSuspensionPoint.onPause();
        showNotifyBar();
    }

    @Override
    protected void onResume() {
        GplayEmulator.setHostActivity(this);
        if (mGplayService != null) {
            mGplayService.onResume();
        }
        super.onResume();
        if(null!=mSuspensionPoint)
            mSuspensionPoint.onResume();
        forceFullScreen();
    }

    @Override
    protected void onDestroy() {
        if (mGplayService != null) {
            mGplayService.onDestroy();
        }
        super.onDestroy();
        if(netStatusReceiver!=null){
            unregisterReceiver(netStatusReceiver);
        }

        // 必须杀死进程, 否者会影响下次游戏运行
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onStop() {
        if (mGplayService != null) {
            mGplayService.onStop();
        }
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (mGplayService != null) {
            mGplayService.onWindowFocusChanged(hasFocus);
        }
        super.onWindowFocusChanged(hasFocus);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                forceFullScreen();
            }
        }, 200);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mGplayService != null) {
            mGplayService.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void share() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(Constants.URL_SHARE_HTML,  "debug", mOrientation ));
        shareIntent.setType("text/html");

        //设置分享列表的标题，并且每次都显示分享列表
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    private void forceFullScreen() {
        mRootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }

    private void showNotifyBar() {
        mRootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void sendToDesktop() {
        JSONObject jsonGameInfo = mGplayService.getGameInfo();
        String iconUrl = jsonGameInfo.optString("icon_url");
        final String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/icon.png";
        Log.d(TAG, "sendToDesktop:");
        Utils.downloadFileInThread(iconUrl, savePath, new com.skydragon.gplay.demo.utils.OnFileDownloadListener() {
            @Override
            public void onFailure() {

            }

            @Override
            public void onSuccess() {
                createShortcut(savePath);
                Utils.removeFile(savePath);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(long downloaded, long total) {

            }
        });
    }

    private void createShortcut(String iconFile) {
        JSONObject jsonGameInfo = mGplayService.getGameInfo();
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, jsonGameInfo.optString("game_name"));
        shortcut.putExtra("duplicate", false);//设置是否重复创建
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        Uri uri = Uri.parse("gplay://gplay_channel_666666");
        intent.setData(uri);
        intent.putExtra("client_id", jsonGameInfo.optString("client_id"));
        intent.putExtra("orientation", jsonGameInfo.optString("orientation"));
        //设置打开游戏Activity的类
        intent.setClass(this, this.getClass());
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        int size = (int) getResources().getDimension(android.R.dimen.app_icon_size);
        Bitmap icon = BitmapFactory.decodeFile(iconFile);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, Bitmap.createScaledBitmap(icon, size, size, false));
        sendBroadcast(shortcut);
    }

    private JSONObject createGameInfoForDebug(String sGameInfo) {
        try{
            JSONObject jsonInfo = new JSONObject(sGameInfo);
            jsonInfo.put("client_id", "testgame");
            JSONObject jsonVerifyInfo = new JSONObject();
            jsonVerifyInfo.put("compatible", 1);
            jsonVerifyInfo.put("visible", 1);
            jsonVerifyInfo.put("maintain", 1);
            jsonVerifyInfo.put("archsupport", 1);
            jsonInfo.put("verifyinfo", jsonVerifyInfo);
            JSONObject jsonChannelInfo = new JSONObject();
            jsonChannelInfo.put("client_id", "gplaydemo");
            jsonChannelInfo.put("client_secret", "gplaydemo");
            jsonInfo.put("channel_config", jsonChannelInfo);
            return jsonInfo;
        } catch( Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getGameOrientation(String jsonStr) {
        try{
            JSONObject jsonObject = new JSONObject(jsonStr);

            return jsonObject.optInt("orientation");
        } catch(Exception e) {
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }
    }

    private ConnectivityManager mConnectivityManager;

    private NetworkInfo netInfo;

    private BroadcastReceiver netStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(null == mGplayService) return;
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                netInfo = mConnectivityManager.getActiveNetworkInfo();
                if(netInfo != null && netInfo.isAvailable()) {

                    /////////////网络连接
                    String name = netInfo.getTypeName();
                    System.out.println("zjf@ net name=" + name);
                    if(netInfo.getType()==ConnectivityManager.TYPE_WIFI){
                        System.out.println("zjf@ net type is wifi");
                        mGplayService.startSilentDownload();

                    }else if(netInfo.getType()==ConnectivityManager.TYPE_ETHERNET){
                        System.out.println("zjf@ net type is ethernet");
                        mGplayService.startSilentDownload();

                    }else if(netInfo.getType()==ConnectivityManager.TYPE_MOBILE){
                        System.out.println("zjf@ net type is mobile");
                        mGplayService.stopSilentDownload();

                    }
                } else {
                    ////////网络断开

                }
            }

        }
    };
}
