package com.skydragon.gplay.demo;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.skydragon.gplay.Gplay;
import com.skydragon.gplay.demo.utils.LogWrapper;
import com.skydragon.gplay.demo.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Html游戏列表示例,渠道上线时请重新定制
 *
 * gamelist2.html:
 *  1. runGplayGame运行游戏
 *  2. 启动GameActivity需要gamekey,orientation参数
 *
 * GplayJavaScriptInterface.java:
 *  1. runGame运行游戏
 *
 * 调用过程gamelist.html -> GplayJavaScriptInterface.java
 *
 * game_share.html:
 * 1. 通过gplay://gplay_channel_渠道号,启动GameActivity(详见AndroidManifest中GameActivity配置)
 * 2. 启动GameActivity需要client_id,orientation参数(详见game_share.html)
 *
 *
 */
public class Html5GameListActivity extends BaseGameListActivity implements View.OnClickListener {

    public static final String TAG = "Html5GameListActivity";

    private GplayJavaScriptInterface mGplayJavascriptInterface = new GplayJavaScriptInterface(this);

    private WebView mWebView = null;

    private String mGameListUrl = "file:///android_asset/gamelist.html";

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogWrapper.i(TAG, "onCreate...");

        setContentView(R.layout.h5homepage);

        createUnityRuntimeEnvironment();

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        // 开启JavaScript支持
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.addJavascriptInterface(mGplayJavascriptInterface, "Gplay");
        this.findViewById(R.id.setting_btn).setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        String productMode = Utils.getSharedPreferences(this).getString("product_mode", Gplay.getProductMode());
        Gplay.setProductMode(productMode);
        loadGameList();
    }

    @Override
    protected void onChannelChange() {
        loadGameList();
    }

    @Override
    protected void onPrepareRuntimeSuccess() {
        //成功回调之后显示Gplay游戏列表
        loadGameList();
    }

    @Override
    protected void onPrepareRuntimeCancel() { }

    @Override
    protected void onPrepareRuntimeFailed() { }

    /**
     *  请求当前渠道游戏列表
     */
    private void loadGameList() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(getGameListUrl());
            }
        });
    }

    private String getGameListUrl() {
        return mGameListUrl + "?chn=" + getChannelCode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.setting_btn) {
            Intent intent = new Intent(getBaseContext(), SettingActivity.class);
            startActivity(intent);
        }
    }

    private void createUnityRuntimeEnvironment() {
        String dataDir = this.getApplicationInfo().dataDir;

        File fRootDir = new File(dataDir, "gplay");
        if(!fRootDir.exists()) {
            fRootDir.mkdirs();
        }

        File fRuntimeLibraryDir = new File(fRootDir, "runtime");
        if(!fRuntimeLibraryDir.exists()) {
            fRuntimeLibraryDir.mkdirs();
        }

        String sRuntimeJarFile = fRuntimeLibraryDir.getAbsolutePath() + "/libruntime.jar";
        extractFileToDestDirFromAssets("unity/libruntime.jar", sRuntimeJarFile);

        File fEngineShareLibraryDir = new File(fRootDir, "engine/unity/5.3/sharelibrary");
        if(!fEngineShareLibraryDir.exists()) {
            fEngineShareLibraryDir.mkdirs();
        }
        String slibGplayFile = fEngineShareLibraryDir.getAbsolutePath() + "/libgplay.so";
        extractFileToDestDirFromAssets("unity/libgplay.so", slibGplayFile);

        String slibMainFile = fEngineShareLibraryDir.getAbsolutePath() + "/libmain.so";
        extractFileToDestDirFromAssets("unity/libmain.so", slibMainFile);

        String slibMomoFile = fEngineShareLibraryDir.getAbsolutePath() + "/libmomo.so";
        extractFileToDestDirFromAssets("unity/libmomo.so", slibMomoFile);

        String slibUnityFile = fEngineShareLibraryDir.getAbsolutePath() + "/libunity.so";
        extractFileToDestDirFromAssets("unity/libunity.so", slibUnityFile);

        File fDiffpatchDir = new File(fRootDir, "diffpatch");
        if(!fDiffpatchDir.exists()) {
            fDiffpatchDir.mkdirs();
        }
        String slibDiffPatchFile = fDiffpatchDir.getAbsolutePath() + "/libdiffpatch.so";
        extractFileToDestDirFromAssets("unity/slibDiffPatchFile.so", slibDiffPatchFile);


        File fEngineJavaLibraryDir = new File(fRootDir, "engine/unity/5.3/javalibrary");
        if(!fEngineJavaLibraryDir.exists()) {
            fEngineJavaLibraryDir.mkdirs();
        }

        String slibUnityRuntimeFile = fEngineJavaLibraryDir.getAbsolutePath() + "/libunityruntime.jar";
        extractFileToDestDirFromAssets("unity/libunityruntime.jar", slibUnityRuntimeFile);
    }

    private void extractFileToDestDirFromAssets(String assetFile, String destFile) {
        AssetManager am = this.getAssets();
        InputStream is = null;
        OutputStream os = null;
        try {
            is = am.open(assetFile);
            os = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int len = -1;
            while((len = is.read(buffer)) != -1 ) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch( Exception e ) {
            e.printStackTrace();
        } finally {
            tryClose(is);
            tryClose(os);
        }
    }

    private void tryClose(InputStream is) {
        try{
            is.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void tryClose(OutputStream os) {
        try{
            os.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}


