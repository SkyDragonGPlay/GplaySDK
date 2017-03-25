package com.skydragon.gplay;

import android.app.Activity;
import android.content.Intent;

import com.skydragon.gplay.callback.ICallback;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by zhangjunfei on 15/12/27.
 */
public interface IGplayService {

    /**
     * 启动游戏
     * @param activity
     * @param appId 游戏标识，Gplay 给渠道分配的ID
     * @param proxy
     */
    public void startGame(final Activity activity, String appId, final IGplayServiceProxy proxy);

    /**
     * 启动游戏
     * @param activity
     * @param appId
     * @param bHostManagerRuntime true 宿主管理Runtime的下载更新 false 由Gplay管理Runtime的下载更新
     * @param proxy
     */
    public void startGame(final Activity activity, final String appId, final boolean bHostManagerRuntime, final IGplayServiceProxy proxy);

    /**
     * 启动游戏,有调用方传入游戏信息
     * @param activity
     * @param jsonGameInfo 数据规范如下:
     * {
     *      "client_id":"",      //游戏idSpidermonkey_panda.zip
     *      "package_name":"",   //包名
     *      "game_name":"",      //游戏名称
     *      "version_name":"",   //版本名称
     *      "orientation":"",    //屏幕方向, android 标准定义, 0 横屏, 1 竖屏
     *      "download_url":"",   //游戏资源下载地址
     *      "webhooks_loginoautho":"",  //游戏登陆验证地址
     *      "verifyinfo": {
     *          "compatible":"", //当前版本sdk是否兼容此游戏,兼容则返回1, 否则返回0
     *          "visible":"",    //此游戏当前是否可见,可见则返回1, 否则返回0
     *          "maintain":"",   //此游戏目前是否处于维护中, 没有维护返回1, 否则返回0
     *          "archsupport":"" //游戏是否支持此cpu架构,支持则返回1, 否则返回0
     *      },  //
     *      "channel_config":"{}" //渠道配置信息,返回游戏在服务端的渠道配置信息
     * }
     * @param proxy
     */
    public void startGame(final Activity activity, final JSONObject jsonGameInfo, final IGplayServiceProxy proxy);

    /**
     * 启动游戏,有调用方传入游戏信息
     * @param activity
     * @param jsonGameInfo
     * {
     *      "client_id":"",      //游戏id
     *      "package_name":"",   //包名
     *      "game_name":"",      //游戏名称
     *      "version_name":"",   //版本名称
     *      "orientation":"",    //屏幕方向
     *      "download_url":"",   //游戏资源下载地址
     *      "webhooks_loginoautho":"",  //游戏登陆验证地址
     * }
     * @param proxy
     */
    public void startGameForDebug(final Activity activity, final JSONObject jsonGameInfo, final IGplayServiceProxy proxy);

    /**
     * 取消打开游戏
     */
    public void cancelStartGame();

    /**
     * 重试打开游戏
     * @param appId
     */
    public void retryStartGame(String appId);

    /**
     * 关闭游戏
     */
    public void closeGame();

    /**
     * 启动静默下载
     */
    void startSilentDownload();

    /**
     * 停止静默下载
     */
    void stopSilentDownload();

    /**
     * 设置Runtime API接口访问的服务器Host 地址, 所有接口path路径都是固定的,但可以更换host地址
     * @param host
     */
    public void setRuntimeHostUrl(String host);

    /**
     * 设置统一SDK接口访问的服务器Host 地址, 所有接口path路径都是固定的,但可以更换host地址
     * @param host
     */
    public void setUnitSDKHostUrl(String host);

    /**
     * 获取游戏信息
     * {
     *     "engine_type":"", //引擎类型
     *     "engine_version":"", //引擎版本
     *     "client_id":"", //游戏ID
     *     "orientation":"", //屏幕方向, 横屏 landscape, 竖屏 portrait
     *     "game_name":"", //游戏名称
     *     "package_name":"", //包名
     *     "version_name":"", //版本名称
     *     "icon_url": ""// 图标下载地址
     * }
     * @return
     */
    JSONObject getGameInfo();

    /**
     * 清除所有游戏缓存
     *
     * @return
     */
    public boolean cleanAllGameCache();

    /**
     * 清除游戏缓存
     *
     * @param tag
     *            游戏gameKey
     * @return
     */
    public boolean cleanGameCache(String tag);

    /**
     * 静默下载游戏开关,需要在startGame之前调用
     * @param enabled
     */
    public void setSilentDownloadEnabled(boolean enabled);

//    // 同步方法调用
//    Object invokeMethodSync(String method, Map<String, Object> args);
//
//    // 异步方法调用
//    void invokeMethodAsync(String method, Map<String, Object> args, ICallback callback);

    /**
     * 生命周期onPause
     */
    public void onPause();

    /**
     * 生命周期onResume
     */
    public void onResume();

    /**
     * 生命周期onStop
     */
    public void onStop();

    /**
     * 生命周期onDestroy
     */
    public void onDestroy();

    /**
     * @param hasFocus
     */
    public void onWindowFocusChanged(boolean hasFocus);

    /**
     * 生命周期onNewIntent
     *
     * @param intent
     */
    public void onNewIntent(Intent intent);

    /**
     * 生命周期onActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * 调用额外异步方法
     * */
    public void invokeMethodAsync(final String method, final Map args, final ICallback callback);

    /**
     * 调用额外方法
     * */
    public Object invokeMethodSync(String method, Map args);
}
