package com.skydragon.gplay;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;

import com.skydragon.gplay.service.IPrepareRuntimeService;


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
public class Html5GameListActivity extends BaseGameListActivity {

    public static final String TAG = "Html5GameListActivity";

    private GplayJavaScriptInterface mGplayJavascriptInterface = new GplayJavaScriptInterface(this);

    private WebView mWebView = null;
    private String mGameListUrl = "file:///android_asset/gamelist.html";

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //设置运行模式, 默认是线上模式, 如果需要设置其他运行模式,需要在运行开始就设置好
        Gplay.setProductMode(Gplay.ONLINE_MODE);

        super.onCreate(savedInstanceState);
        setContentView(Utils.getLayout(this, "gplay_h5homepage"));

        mWebView = (WebView) findViewById(Utils.getResourceId(this, "gplay_webview"));
        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        // 开启JavaScript支持
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.addJavascriptInterface(mGplayJavascriptInterface, "Gplay");

        loadGameList();
    }

    @Override
    protected void onChannelChange() {
        loadGameList();
    }


    @Override
    protected void onPrepareRuntimeSuccess() {
        //成功回调之后显示Gplay游戏列表
//        loadGameList();
    }

    @Override
    protected void onPrepareRuntimeCancel() {

    }

    @Override
    protected void onPrepareRuntimeFailed() {

    }

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
        return mGameListUrl + "?channel_code=" + Constants.DEFAULT_CHANNEL_ID;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}


