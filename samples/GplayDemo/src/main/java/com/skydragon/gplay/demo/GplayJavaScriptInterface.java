package com.skydragon.gplay.demo;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.skydragon.gplay.Gplay;
import com.skydragon.gplay.demo.service.PreDownGameService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * 与Html游戏大厅交互类
 */
public class GplayJavaScriptInterface {

    private static final String TAG = "GplayJSInterface";
    Handler mHandler = new Handler(Looper.getMainLooper());
    private Html5GameListActivity mActivity;
    public  String runGameCId;

    GplayJavaScriptInterface(Html5GameListActivity context) {
        mActivity = context;
    }

    /**
     * html游戏大厅点击游戏时调用该函数
     * 
     * @param jsonStr {
     *                "client_id":"", //游戏标识
     *                "orientation":"" //运行屏幕方向
     *                }
     */
    @JavascriptInterface
    public void runGame(final String jsonStr) {
        Log.d(TAG, "GplayJavaScriptInterface runGame called!!!");

        try {
            final JSONObject jsonObject = new JSONObject(jsonStr);
            runGameCId = jsonObject.optString("client_id");
            Intent i = new Intent(mActivity,PreDownGameService.class);
            i.putExtra("RUNGAMECID",runGameCId);
            i.putExtra("STOP_PREDOWN","stop");
            mActivity.startService(i);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mActivity.stopDownload();
                    Intent intent = new Intent(mActivity, GameActivity.class);
                    intent.putExtra("client_id", jsonObject.optString("client_id"));
                    intent.putExtra("orientation", jsonObject.optInt("orientation"));
                    mActivity.startActivity(intent);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分享游戏链接
     * @param url
     */
    public void shareGame(String url) {
        // scheme://host?client_id=xxx&orientation=xxx,
        // 其中scheme,host为AndroidManifest.xml中配置的值
        // 示例: gplay://gplay_channel_666666?client_id=xxx&orientation=landscape
        // 链接展现形式可由渠道自定义,如: 制作一个正常的html页面,由html页面内部进行跳转以上url
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager cmb = (android.content.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clipData = android.content.ClipData
                    .newPlainText("text label", url.trim());
            cmb.setPrimaryClip(clipData);
        } else {
            // 老版本系统，使用旧的 API
            @SuppressWarnings("deprecation")
            android.text.ClipboardManager cmb = (android.text.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(url.trim());
        }

        Toast.makeText(mActivity, "分享链接已拷贝", Toast.LENGTH_LONG).show();
    }


    @JavascriptInterface
    public String getGameListUrl() {
        return Gplay.getGameListUrl();
    }

    @JavascriptInterface
    public void preDownGplayGame(String object){
            Log.d(TAG, "GplayJavaScriptInterface preDownGame called!!!");
            Intent i = new Intent(mActivity,PreDownGameService.class);
            i.putExtra("GAME_KEY",object);
            mActivity.startService(i);
    }

}