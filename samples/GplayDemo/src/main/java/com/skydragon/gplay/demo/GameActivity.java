package com.skydragon.gplay.demo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
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
import com.skydragon.gplay.callback.ICallback;
import com.skydragon.gplay.callback.OnPrepareRuntimeListener;
import com.skydragon.gplay.channel.plugin.GplayChannelPaySDKPlugin;
import com.skydragon.gplay.channel.plugin.h5.GplayChannelPayH5SDKPlugin;
import com.skydragon.gplay.demo.service.PreDownGameService;
import com.skydragon.gplay.demo.ui.LoadingProgressDialog;
import com.skydragon.gplay.demo.ui.ProgressView;
import com.skydragon.gplay.demo.ui.TipsManager;
import com.skydragon.gplay.demo.utils.Utils;
import com.skydragon.gplay.sprite.GplaySpriteButton;
import com.skydragon.gplay.sprite.GplaySpritePoint;
import com.skydragon.gplay.sprite.OnGplayButtonAction;
import com.skydragon.gplay.thirdsdk.IChannelSDKServicePlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameActivity: 该Activity实现了包含准备Runtime运行环境,启动游戏等逻辑. 开发者可以参考该类拓展自己的业务逻辑
 * 注意: 单独进程管理该Activity,结束游戏时关闭进程,具体配置请参考AndroidMainifest.xml
 * <p/>
 * <p/>
 * 1. 加载运行环境,调用Gplay中prepareRuntime方法,运行环境加载成功之后会调用onPrepareRuntimeSuccess回调方法
 * 2. 启动游戏,通过onPrepareRuntimeSuccess方法可获取到IGplayService实现对象,然后调用startGame方法来启动游戏
 * 3. 生命周期,在Activity的生命周期方法中调用IGplayService中对应方法,如onResume
 */
public class GameActivity extends Activity implements OnPrepareRuntimeListener, IGplayServiceProxy {

    private static final String TAG = "GameActivity";

    private final String METHOD_CAPTURE_SCREEN = "CAPTURE_SCREEN";
    private final String METHOD_PRELOAD_SCENES_SET_CALLBACK = "PRELOAD_SCENES_SET_CALLBACK";
    private final String METHOD_PRELOAD_SCENES_RETRY = "PRELOAD_SCENES_RETRY";
    private final String METHOD_PRELOAD_SCENES_ONFINISH = "PRELOAD_SCENES_ONFINISH";
    private final String METHOD_GET_VERSION_CONFIGS = "GET_VERSION_CONFIGS";
    private final String KEY_U_FILE_NAME = "FILE_NAME";
    private final String KEY_FILE_NAME = "file_name";
    private final String KEY_RESULT_CODE = "result_code";
    private final String KEY_ERROR_CODE = "error_code";
    private final String KEY_SCENE_NAME = "scene_name";
    private final String KEY_PERCENT = "percent";
    private final String KEY_DOWNLOAD_SPEED = "download_speed";
    private final String KEY_STAGE = "stage";

    private String mChannelID;
    private String mCacheDir;
    private String mClientId;
    private String mProductMode;
    private String mScreenShotsDir;
    private int mOperateSdkMode;
    private boolean mUseChannelProgress;
    private int mOrientation;
    private String mGameInfoJsonStr;
    private boolean mIsGameStarted;
    private Context mActivity;
    private FrameLayout mRootLayout = null;

    private ProgressView mProgressView;

    private IGplayService mGplayService;

    private TipsManager mTipsManager;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mChannelID = Utils.getSharedPreferences(this).getString("channel_id", Constants.DEFAULT_CHANNEL_ID);
        mCacheDir = Utils.getSharedPreferences(this).getString("rootpath", Environment.getExternalStorageDirectory() + "/gplay_demo");
        mScreenShotsDir = Utils.getSharedPreferences(this).getString("rootpath", Environment.getExternalStorageDirectory() + "/gplay_demo/screenShots");
        mProductMode = Utils.getSharedPreferences(this).getString("product_mode", Gplay.getProductMode());
        mOperateSdkMode = Integer.parseInt(Utils.getSharedPreferences(this).getString("sdk_choose", "0"));
        int channelProgress = Integer.parseInt(Utils.getSharedPreferences(this).getString("channel_progress", "0"));
        mUseChannelProgress = (channelProgress == 0) ? false : true;
        mActivity=this;
        //设置运行环境
        Gplay.setProductMode(mProductMode);

        Utils.ensureFileExist(mCacheDir);
        mIsGameStarted = false;
        Intent intent = getIntent();
        String scheme = intent.getScheme();
        if (!TextUtils.isEmpty(scheme)) {
            // 打开分享链接的处理逻辑
            Uri uri = intent.getData();
            String clientId = uri.getQueryParameter("client_id");
            if (null != clientId) {
                mClientId = clientId;
            } else {
                mClientId = intent.getStringExtra("client_id");
            }

            String orientation = uri.getQueryParameter("orientation");
            if (null != orientation) {
                mOrientation = Integer.parseInt(orientation);
            } else {
                mOrientation = intent.getIntExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        } else {
            mClientId = intent.getStringExtra("client_id");
            mOrientation = intent.getIntExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            mGameInfoJsonStr = intent.getStringExtra("gameinfo");
//            mGameInfoJsonStr = "{\"package_name\":\"com.gplay.gplayjssodemo\",\"game_name\":\"MoonWarriorV310\",\"version_name\":\"1.0\",\"version_code\":1,\"type\":0,\"download_url\":\"http:\\/\\/download.skydragon-inc.cn\\/games\\/moon_1603070309\",\"webhooks_loginoauth\":\"http:\\/\\/sandbox.api.skydragon-inc.cn\\/user\\/UnitLoginOauth\",\"client_id\":\"moonwid\",\"orientation\":1,\"verifyinfo\":{\"compatible\":1,\"visible\":1,\"maintain\":1},\"channel_config\":null}";
        }
        mRootLayout = new FrameLayout(this);

        // 准备 runtime 环境, 在 onPrepareRuntimeSuccess 回调中保存 GplayServic 实现, 并启动游戏
        prepareRuntime();

        Log.d(TAG, "mOrientation=" + mOrientation);
        setGameScreenOrientation(mOrientation);

        super.onCreate(savedInstanceState);

        setContentView(mRootLayout);
    }

    private void prepareRuntime() {
        // 显示加载页面, 准备 Runtime 运行环境, 设置渠道 SDK 代理
        mProgressView = new ProgressView(this);
        if (mOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mProgressView.setBackgroundResource(R.drawable.bg_default_landscape);
        } else {
            mProgressView.setBackgroundResource(R.drawable.bg_default_portrait);
        }
        mRootLayout.addView(mProgressView);

        IChannelSDKServicePlugin ichannelServicePluginProxy = new GplayChannelPaySDKPlugin();

        if (mOperateSdkMode == Constants.USE_GPLAY_H5_PAYSDK) {
            ichannelServicePluginProxy = new GplayChannelPayH5SDKPlugin();
        }
        Gplay.prepareRuntime(this, mChannelID, mCacheDir, ichannelServicePluginProxy, this);
    }

    /**
     * 创建屏幕截图， 保存到自定的文件。
     */
    private void createScreenShots(String fileName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(KEY_U_FILE_NAME, mScreenShotsDir + File.separator + fileName);
        if (mGplayService != null)
            mGplayService.invokeMethodAsync(METHOD_CAPTURE_SCREEN, params, new ICallback() {
                @Override
                public Object onCallback(String from, Map<String, Object> args) {
                    if (from != null && from.equals(METHOD_CAPTURE_SCREEN)) {
                        Integer resultCode = (Integer) args.get(KEY_RESULT_CODE);
                        String fileName = (String) args.get(KEY_FILE_NAME);
                        Toast.makeText(GameActivity.this, "code : " + resultCode + "  filename : " + fileName, Toast.LENGTH_LONG).show();
                    }
                    return null;
                }
            });
    }

    /**
     * 获取游戏信息
     */
    private void fetchGameInfo() {
        if (mGplayService != null) {
            JSONObject resultJsonObj = (JSONObject) mGplayService.invokeMethodSync(METHOD_GET_VERSION_CONFIGS, null);
            if (resultJsonObj != null) {
                try {
                    Toast.makeText(GameActivity.this, resultJsonObj.toString(2), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    boolean isShowProgress = false;

    private void preLoadScenesCallback() {

        if (mGplayService != null)
            mGplayService.invokeMethodAsync(METHOD_PRELOAD_SCENES_SET_CALLBACK, null, new ICallback() {
                @Override
                public Object onCallback(String from, Map<String, Object> args) {
                    if (from != null && from.equals(METHOD_PRELOAD_SCENES_SET_CALLBACK)) {
                        Integer resultCode = (Integer) args.get(KEY_RESULT_CODE);
                        Integer errorCode = (Integer) args.get(KEY_ERROR_CODE);
                        String sceneName = (String) args.get(KEY_SCENE_NAME);
                        Float percent = (Float) args.get(KEY_PERCENT);
                        Integer stage = (Integer) args.get(KEY_STAGE);
                        Float downloadSpeed = (Float) args.get(KEY_DOWNLOAD_SPEED);

                        if (!isShowProgress) {
                            LoadingProgressDialog.showLoadingDialog(GameActivity.this, "Loading ...");
                            LoadingProgressDialog.onProgress(0);
                            isShowProgress = true;
                        }

                        LoadingProgressDialog.onProgress(percent);


                        if (resultCode == 1 && percent == 100) {
                            LoadingProgressDialog.closeLoadingDialog();
                            preLoadScenesOnFinished();
                            isShowProgress = false;
                        }
                        Log.v(TAG, "PreLoadScenesCallback code " + resultCode + "  , errorcode : " + errorCode + "  ,  percent " + percent + "  , speed " + downloadSpeed + " sceneName " + sceneName + "  , stage : " + stage);

                    }
                    return null;
                }
            });
    }

    private void preloadScenesRetry() {
        if (mGplayService != null)
            mGplayService.invokeMethodSync(METHOD_PRELOAD_SCENES_RETRY, null);
    }

    private void preLoadScenesOnFinished() {
        if (mGplayService != null)
            mGplayService.invokeMethodSync(METHOD_PRELOAD_SCENES_ONFINISH, null);
    }

    private void startGame() {
        // 会先请求游戏信息, 判断是否需要下载引擎jar 包, 补丁
        // 检查 boot 包完整以后, 启动游戏.
        mTipsManager = new TipsManager(GameActivity.this, mGplayService);
        mTipsManager.setClientId(mClientId);

        if (null != mGameInfoJsonStr && !mGameInfoJsonStr.trim().equals("")) {
            JSONObject jsonGameInfo = null;
            try {
                jsonGameInfo = new JSONObject(mGameInfoJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mGplayService.startGame(this, jsonGameInfo, this);
        } else {
            //是否开启静默下载,默认是开启
            mGplayService.setSilentDownloadEnabled(false);
            //宿主管理模式
//            mGplayService.startGame(this, mClientId, true, this);

            //Gplay管理模式
            mGplayService.startGame(this, mClientId, this);
        }
    }

    @Override
    public void onPrepareRuntimeStart() {
    }

    /**
     * 下载Runtime SDK
     *
     * @param downloadedSize
     * @param totalSize
     */
    @Override
    public void onPrepareRuntimeProgress(long downloadedSize, long totalSize) {

    }

    @Override
    public void onPrepareRuntimeSuccess(IGplayService service) {
        mGplayService = service;
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
        Log.d(TAG, "OnGameDownloadListener onDownloadProgress : " + downloadedPercent + " " + downloadSpeed);
        mProgressView.updateProgress(downloadedPercent, downloadSpeed);
    }

    @Override
    public void onDownloadGameSuccess() {
        Log.d(TAG, "OnGameDownloadListener onDownloadGameSuccess " + mUseChannelProgress);
        if (mUseChannelProgress) {
            preLoadScenesCallback();
        }
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
        mProgressView = null;
        if (mGplayService != null) {
            setResCompleteCallback(mGplayService);
        }
        // 可根据自身需求定制添加
        showSuspensionView();
        fetchGameInfo();
    }

    public void setResCompleteCallback(IGplayService iGplayService) {
        Log.d(TAG, "check ResComplete");
        iGplayService.invokeMethodAsync("SET_RES_COMPLETE_CALLBACK", null, new ICallback() {
            @Override
            public Object onCallback(String from, Map<String, Object> args) {
                boolean isAll = (boolean) args.get("RESOURCE_COMPLETE");
                if (isAll) {
                    Log.d(TAG, "Res is complete,continue predown");
                    Intent i = new Intent(mActivity, PreDownGameService.class);
                    i.putExtra("CONTINUE_PREDOWN","continue_predown");
                    mActivity.startService(i);
                }
                return null;
            }
        });
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

        int whitePointSize = (int) (48 * this.getResources().getDisplayMetrics().density);
        int distance = (int) (28 * this.getResources().getDisplayMetrics().density);
        GplaySpritePoint whitePoint = new GplaySpritePoint(this, mRootLayout, screenWidth, screenHeight / 4, dWhitePoint, whitePointSize, distance);
        mSuspensionPoint = whitePoint;

        int buttonSize = (int) (32 * this.getResources().getDisplayMetrics().density);

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


        Drawable dScreenShot = this.getResources().getDrawable(R.drawable.gplay_screenshot);
        GplaySpriteButton suspensionScreenshotButton = new GplaySpriteButton(dScreenShot, "截图", buttonSize);
        suspensionScreenshotButton.setGplayButtonAction(new OnGplayButtonAction() {
            @Override
            public void onInvoke() {
                createScreenShots("screenshot.jpeg");
            }
        });
        suspensionScreenshotButton.init(this, 2, 100, 100);
        whitePoint.addSuspendButton(suspensionScreenshotButton);

        Drawable dCloseButton = this.getResources().getDrawable(R.drawable.gplay_close);
        GplaySpriteButton suspensionCloseButton = new GplaySpriteButton(dCloseButton, "退出", buttonSize);
        suspensionCloseButton.setGplayButtonAction(new OnGplayButtonAction() {
            @Override
            public void onInvoke() {
                GameActivity.this.finish();
            }
        });
        suspensionCloseButton.init(this, 3, 100, 100);
        whitePoint.addSuspendButton(suspensionCloseButton);

        whitePoint.setExpandBackground(this.getResources().getDrawable(R.drawable.transparent_background));

        whitePoint.start();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (!mIsGameStarted && mTipsManager != null) {
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
        if (null != mSuspensionPoint)
            mSuspensionPoint.onNewIntent();
        forceFullScreen();
    }

    @Override
    protected void onPause() {
        if (mGplayService != null) {
            mGplayService.onPause();
        }
        super.onPause();
        if (null != mSuspensionPoint)
            mSuspensionPoint.onPause();
        showNotifyBar();
    }

    @Override
    protected void onResume() {
        if (mGplayService != null) {
            mGplayService.onResume();
        }
        super.onResume();
        if (null != mSuspensionPoint)
            mSuspensionPoint.onResume();
        forceFullScreen();
    }

    @Override
    protected void onDestroy() {
        if (mGplayService != null) {
            mGplayService.onDestroy();
        }

        Intent ir = new Intent(mActivity,PreDownGameService.class);
        ir.putExtra("RUNGAMECID","ID is null");
        mActivity.startService(ir);

        if(mProgressView != null){
            //进入游戏时加载首包资源会停止预下载，所以1.进入游戏后全部游戏资源加载完整，继续预下载，2.首包资源加载未完成退出游戏时继续预下载
            Intent it = new Intent(this, PreDownGameService.class);
            it.putExtra("CONTINUE_PREDOWN", "continue_predown");
            this.startService(it);
        }

        if (mProgressView != null && mClientId != null && isOpenBGHang()) {
            //后台挂起下载
            Intent i = new Intent(this, PreDownGameService.class);
            i.putExtra("GAME_KEY", mClientId);
            this.startService(i);
        }


        super.onDestroy();
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(Constants.URL_SHARE_HTML, mClientId, mOrientation));
        shareIntent.setType("text/html");

        //设置分享列表的标题，并且每次都显示分享列表
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    private void forceFullScreen() {
        if (Build.VERSION.SDK_INT >= 11) {
            mRootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private void showNotifyBar() {
        if (Build.VERSION.SDK_INT >= 11) {
            mRootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
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
        //666666 改成接入APP分配的渠道号
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

    //判断是否打开后台挂起下载
    private boolean isOpenBGHang() {
        SharedPreferences mSettings = Utils.getSharedPreferences(this);

        if (mSettings.getString("bghang_option", "0").equals("1")) {
            return true;
        }
        return false;
    }
}
