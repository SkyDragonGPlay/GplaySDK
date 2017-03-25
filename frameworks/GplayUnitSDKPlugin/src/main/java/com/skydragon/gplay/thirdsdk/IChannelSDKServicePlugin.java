package com.skydragon.gplay.thirdsdk;

import android.app.Activity;
import android.content.Intent;
import android.webkit.ValueCallback;

import org.json.JSONObject;

/**
 * 渠道服务接口，接入APP需要实现此接口
 */
public interface IChannelSDKServicePlugin {
    int USER_LOGIN_RESULT_SUCCESS = 10000;
    int USER_LOGIN_RESULT_FAIL = 10001;
    int USER_LOGIN_RESULT_CANCEL = 10002;
    int USER_LOGOUT_RESULT_SUCCESS = 10003;
    int USER_LOGOUT_RESULT_FAIL = 10004;
    int USER_REGISTER_RESULT_SUCCESS = 10005;
    int USER_REGISTER_RESULT_FAIL = 10006;
    int USER_REGISTER_RESULT_CANCEL = 10007;
    int USER_BIND_RESULT_SUCESS = 10008;
    int USER_BIND_RESULT_CANCEL = 10009;
    int USER_BIND_RESULT_FAILED = 10010;
    int USER_RESULT_NETWROK_ERROR = 10011;
    int USER_RESULT_USEREXTENSION = 19999;

    int PAY_RESULT_SUCCESS = 20000;
    int PAY_RESULT_FAIL = 20001;
    int PAY_RESULT_CANCEL = 20002;
    int PAY_RESULT_INVALID = 20003;
    int PAY_RESULT_NETWORK_ERROR = 20004;
    int PAY_RESULT_NOW_PAYING = 20005;
    int PAY_REQUEST_CHARGE_SUCCESS = 20006;
    int PAY_REQUEST_CHARGE_FAILED = 20007;
    int PAY_RESULT_PAYEXTENSION = 29999;

    int SHARE_RESULT_SUCCESS = 30000;
    int SHARE_RESULT_FAIL = 30001;
    int SHARE_RESULT_CANCEL = 30002;
    int SHARE_RESULT_NETWORK_ERROR = 30003;
    int SHARE_RESULT_SHAREREXTENSION = 39999;

    int SHORTCUT_RESULT_SUCCESS = 40000;
    int SHORTCUT_RESULT_FAILED = 40001;

    int RESULT_NOT_SUPPORT_FUNCTION = -1;

    String APPKEY = "client_id";

    /**
     * 订单号
     */
    String ORDER_ID = "order_id";

    /**
     * 商品ID
     */
    String PRODUCT_ID = "product_id";

    /**
     * 订单总金额
     */
    String PRODUCT_AMOUNT = "product_amount";

    /**
     * 商品数量
     */
    String PRODUCT_COUNT = "product_count";

    /**
     * 商品单价
     */
    String PRODUCT_PRICE = "product_price";

    /**
     * 商品名称
     */
    String PRODUCT_NAME = "product_name";

    /**
     * 商品描述
     */
    String PRODUCT_DESC = "product_desc";

    /**
     * 玩家角色ID
     */
    String GAME_ROLE_ID = "game_user_id";

    /**
     * 玩家角色名称
     */
    String GAME_ROLE_NAME = "game_user_name";

    /**
     * 服务器ID
     */
    String SERVER_ID = "server_id";

    /**
     * 服务器名称
     */
    String SERVER_NAME = "server_name";

    String EXT = "private_data";

    /**
     * 渠道交易信息
     */
    String CHANNEL_CHARGE = "channel_charge";

    /**
     * 渠道用户ID
     */
    String CHANNEL_USER_ID = "user_id";

    /**
     * 渠道号
     */
    String CHANNEL_CODE = "channel_code";

    String EXTRA = "extra";

    /**
     * @param act   activity context
     * @param params 包含了游戏在此接入APP中申请的所有参数数据,请根据渠道本身分配的参数信息来获取对应的值
     *
     * 注:如果不清楚包含哪些键值,请联系Gplay开发人员
     * 具体实例可参考Gplay 第三方SDK 服务实现类GplayChannelServicePluginProxy
     */
    void init(Activity act, JSONObject params);

    /**
     * Gplay IChannelSDKBridge,通过这个对象获取交易凭证信息
     * @param bridge
     */
    void setChannelSDKBridge(IChannelSDKBridge bridge);

    /***
     * 登陆
     * JSONObject 包含如下数据 {}
     * 调⽤用结束后通过callback通知runtime,callback中的JSONObject包含如下数据:
     * {
     *      "result":, //参考ChannelSDKWrapper中返回码
     *      "msg": "", //传回的提⽰示信息
     *      "data": {} //渠道服务端对登陆用户进行二次验证需要的数据,请按照渠道服务端规范来传递对应的键值数据
     * }
     *
     * 具体实例可参考Gplay 第三方SDK 服务实现类GplayChannelServicePluginProxy
     */
    void login(JSONObject jsonObject, final ValueCallback<JSONObject> callback);

    /***
     * JSONObject 包含如下数据 {}
     * 调⽤用结束后通过callback通知runtime。Callback中的JSONObject包含如下数据:
     * {
     *      "result":, //result为0说明函数执⾏行成功,否则失败
     *      "msg": "", //传回的提⽰示信息
     * }
     */
    void logout(JSONObject jsonObject, final ValueCallback<JSONObject> callback);

    /***
     * ⽀支付
     * JSONObject 包含如下数据,
     * {
     *      "order_id" : "" //订单ID
     *      "product_amount": "", //商品总金额
     *      "product_name" : "", //商品名称
     *      "product_desc":"", //商品描述
     *      "product_count":"", //商品数量
     *      "product_price":"", //商品单价
     *      "game_user_id":"", //玩家角色ID
     *      "game_user_name":"", //玩家角色名称
     *      "server_id":"", //服务器ID
     *      "server_name":"", //服务器名称
     *      "channel_charge": "", //渠道服务端生成商品交易信息,这种情况只针对在支付之前需要在服务端生成订单交易信息的渠道
     *      "extra" : "" //扩展数据
     * } 调⽤用结束后通过callback通知runtime,Callback中的JSONObject包含如下数据:
     * {
     *      "result":, //参考ChannelSDKWrapper中返回码
     *      "msg": "", //传回的提⽰示信息
     *      "pay_real_amount": //实际支付金额,可选.
     * }
     ***/
    void pay(JSONObject jsonObject, final ValueCallback<JSONObject> callback);

    /***
     * 分享
     * JSONObject 包含如下数据:
     * {
     *      "url":'', //分享后点击的⻚页⾯面url
     *      "title":'',//分享界⾯面的标题
     *      "text":'',//分享内容
     *      "img_url":'',//分享内容插图url
     *      "img_title":'',//分享内容插图的标题
     * } 调⽤用结束后通过callback通知runtime。Callback中的JSONObject包含如下数据:
     * {
     *      "result": , //分享结果,包括成功、取消、失败、⺴⽹网络错误等类型
     *      "msg": "", //传回的提⽰示信息
     * }
     ***/
    void share(JSONObject jsonObj, ValueCallback<JSONObject> callback);

    /***
     * 创建游戏的桌面快捷方式
     * {
     *     "engine_type":"", //引擎类型
     *     "engine_version":"", //引擎版本
     *     "client_id":"", //游戏ID
     *     "orientation":"", //屏幕方向, 横屏 landscape, 竖屏 portrait
     *     "game_name":"", //游戏名称
     *     "icon_url": ""// 图标下载地址
     * }
     * 调⽤用结束后通过callback通知runtime,Callback中的JSONObject包含如下数据:
     * {
     *      "result":, //result为0说明函数执⾏行成功,否则失败
     *      "msg": "", //传回的提⽰示信息
     * }
     ***/
    void createShortcut(JSONObject jsonObj, ValueCallback<JSONObject> callback);

    /**
     * 是否支持某个函数
     * @param method 函数名称
     * @return true 表示支持这个函数 false  表示不支持这个函数
     */
    boolean isFunctionSupported(String method);

    /**
     * 对应Activity onResume
     */
    void onActivityResume();

    /**
     * 对应Activity onRestart
     */
    void onActivityRestart();

    /**
     * 对应Activity onPause
     */
    void onActivityPause();

    /**
     * 对应Activity onStop
     */
    void onActivityStop();

    /**
     * 对应onDestroy
     */
    void onActivityDestroy();

    /**
     * 对应onNewIntent
     * @param intent
     */
    void onActivityNewIntent(Intent intent);

    /**
     * 对应onActivityResult
     * @param requestCode
     * @param resultCode
     * @param data
     */
    void onActivityResultWrapper(int requestCode, int resultCode, Intent data);

    /**
     * 调用渠道提供的方法
     * @param method 方法名称
     * @param jsonParams 参数
     * @return 对于简单类型,直接返回对应的字符串值, 如果是复杂类型,需要生成json字符串
     */
    String invokeSyncExtraMethods(String method, JSONObject jsonParams);


    /**
     * 调用渠道提供的方法
     * @param method 方法名称
     * @param jsonParams 参数
     * @param callback 回调方法
     * @return
     */
    void invokeAsynExtraMethods(String method, JSONObject jsonParams, IChannelSDKCallback callback);

    /**
     * 游戏退出时会调用插件的destroy方法
     */
    void destroy();
}
