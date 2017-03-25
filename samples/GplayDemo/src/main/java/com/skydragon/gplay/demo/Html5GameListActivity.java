package com.skydragon.gplay.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.skydragon.gplay.Gplay;
import com.skydragon.gplay.demo.utils.LogWrapper;
import com.skydragon.gplay.demo.utils.Utils;

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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                updateAPNType();
                if(mCurrNetworkType != -1)
                    loadGameList();
                LogWrapper.i(TAG, "the network state has changed:" + mCurrNetworkType);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogWrapper.i(TAG, "onCreate...");

        setContentView(R.layout.h5homepage);

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

        //监听网络状态
        updateAPNType();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String productMode = Utils.getSharedPreferences(this).getString("product_mode", Gplay.getProductMode());
        Gplay.setProductMode(productMode);

        if(mCurrNetworkType != -1)
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

    private int mCurrNetworkType = -1;

    private void updateAPNType() {
        mCurrNetworkType = -1;

        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null) {
            return;
        }

        mCurrNetworkType = networkInfo.getType();
    }
}


