package com.skydragon.gplay.channel.plugin.gplaypaysdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.ValueCallback;

import com.skydragon.gplay.demo.utils.Utils;
import com.skydragon.gplay.paysdk.GplayPaySDK;
import com.skydragon.gplay.paysdk.OAuthData;
import com.skydragon.gplay.paysdk.PayData;
import com.skydragon.gplay.thirdsdk.IChannelSDKBridge;
import com.skydragon.gplay.thirdsdk.IChannelSDKCallback;
import com.skydragon.gplay.thirdsdk.IChannelSDKServicePlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GplayChannelServicePluginProxy implements IChannelSDKServicePlugin {

    private static final String TAG = "ChannelSDKPluginProxy";

    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";

    private Activity mActivity;

    @Override
    public void init(Activity activity, JSONObject jsonObject) {
        Log.d(TAG, "init Channel service Plugin proxy");
        mActivity = activity;
        String appId = jsonObject.optString(CLIENT_ID);
        String appSecret = jsonObject.optString(CLIENT_SECRET);
        GplayPaySDK.init(activity, appId, appSecret);
    }

    @Override
    public void setChannelSDKBridge(IChannelSDKBridge iChannelSDKBridge) {
        
    }

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
    @Override
    public void createShortcut(final JSONObject jsonObj, final ValueCallback<JSONObject> callback) {
        Log.d(TAG, "createShortcut: ");
        final String engineType = jsonObj.optString("engine_type");
        final String engineVersion = jsonObj.optString("engine_version");
        final String clientId = jsonObj.optString("client_id");
        final String name = jsonObj.optString("game_name");
        final String orientation = jsonObj.optString("orientation");
        final String iconUrl = jsonObj.optString("icon_url");

        final String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/icon.jpg";
        downloadFile(iconUrl, savePath, new OnFileDownloadListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObj.put("result", IChannelSDKServicePlugin.SHORTCUT_RESULT_FAILED);
                    jsonObj.put("msg", "icon download failed!");
                    callback.onReceiveValue(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgress(long downloaded, long total) {

            }

            @Override
            public void onSuccess() {
                createShortcut(name, savePath, clientId, orientation, engineType, engineVersion, new IChannelSDKCallback() {
                    @Override
                    public void onCallback(int resultCode, String resultJsonMsg) {
                        try {
                            JSONObject jsonObject = new JSONObject(resultJsonMsg);
                            callback.onReceiveValue(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            Utils.removeFile(savePath);
                        }
                    }
                });
            }
        });

    }

    @Override
    public void login(JSONObject jsonObject, final ValueCallback<JSONObject> callback) {
        GplayPaySDK.login(mActivity, new GplayPaySDK.OAuthResponse() {
            @Override
            public void onResponse(OAuthData data) {
                try {
                    JSONObject jsonResult = loginWrapper(data);
                    callback.onReceiveValue(jsonResult);
                } catch (Exception e) {
                    JSONObject jsonError = createUnexceptionJSON(e);
                    callback.onReceiveValue(jsonError);
                }
            }
        });
    }

    @Override
    public void logout(JSONObject jsonObject, ValueCallback<JSONObject> callback) {
        Log.d(TAG, "The channel sdk don't support logout!");
    }

    @Override
    public void pay(final JSONObject jsonObject, final ValueCallback<JSONObject> callback) {
        Log.d(TAG, "pay: ");
        String order = jsonObject.optString("order_id");
        String amount = jsonObject.optString("product_amount");
        String name = jsonObject.optString("product_name");
        String desc = jsonObject.optString("product_desc");
        String extra = jsonObject.optString("extra");
        GplayPaySDK.pay(mActivity, order, amount, name, desc, extra, new GplayPaySDK.PayResponse() {
            @Override
            public void onResponse(PayData data) {
                try {
                    JSONObject jsonResult = payWrapper(data);
                    callback.onReceiveValue(jsonResult);
                } catch (Exception e) {
                    JSONObject jsonError = createUnexceptionJSON(e);
                    callback.onReceiveValue(jsonError);
                }
            }
        });
    }

    @Override
    public void share(JSONObject jsonObj, ValueCallback<JSONObject> callback) {
        Log.d(TAG, "The channel sdk don't support share!");
    }

    @Override
    public boolean isFunctionSupported(String method) {
        if(method.equalsIgnoreCase("register")) {
            return true;
        } else if(method.equalsIgnoreCase("bind")) {
            return true;
        } else if(method.equalsIgnoreCase("getUid")) {
            return true;
        } else if(method.equalsIgnoreCase("isTrial")) {
            return true;
        } else if(method.equalsIgnoreCase("getVersion")) {
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResume() {
        Log.d(TAG, "onActivityResume called.");
        GplayPaySDK.onResume(mActivity);
    }

    @Override
    public void onActivityRestart() {
        Log.d(TAG, "onActivityRestart called.");
    }

    @Override
    public void onActivityPause() {
        Log.d(TAG, "onActivityPause called.");
        GplayPaySDK.onPause(mActivity);
    }

    @Override
    public void onActivityStop() {
        Log.d(TAG, "onActivityStop called.");
    }

    @Override
    public void onActivityDestroy() {
        Log.d(TAG, "onActivityDestroy called.");
    }

    @Override
    public void onActivityNewIntent(Intent intent) {
        Log.d(TAG, "onActivityNewIntent called.");
    }

    @Override
    public void onActivityResultWrapper(int requestCode, int resultCode, Intent data) {
        GplayPaySDK.onActivityResult(mActivity, requestCode, resultCode, data);
    }

    @Override
    public void invokeAsynExtraMethods(String method, JSONObject jsonParams, IChannelSDKCallback callback) {
        if(null == callback) return;
        if(method.equalsIgnoreCase("register")) {
            register(callback);
        } else if(method.equalsIgnoreCase("bind")) {
            bind(callback);
        } else {
            JSONObject jsonObject = createUnsupportResponse(method);
            callback.onCallback(IChannelSDKServicePlugin.RESULT_NOT_SUPPORT_FUNCTION, jsonObject.toString());
        }
    }

    @Override
    public String invokeSyncExtraMethods(String method, JSONObject jsonParams) {
        if(method.equalsIgnoreCase("getUid")) {
            return GplayPaySDK.getUid();
        } else if(method.equalsIgnoreCase("isTrial")) {
            return Boolean.toString(GplayPaySDK.isTrial());
        } else if(method.equalsIgnoreCase("getVersion")) {
            return String.valueOf(GplayPaySDK.getVersion());
        }
        return null;
    }


    private void bind(final IChannelSDKCallback callback) {
        GplayPaySDK.bind(mActivity, new GplayPaySDK.OAuthResponse() {
            @Override
            public void onResponse(OAuthData data) {
                try{
                    JSONObject jsonObject = bindWrapper(data);
                    callback.onCallback(jsonObject.optInt("result"), jsonObject.toString());
                } catch(Exception e) {
                    JSONObject jsonError = createUnexceptionJSON(e);
                    callback.onCallback(IChannelSDKServicePlugin.USER_REGISTER_RESULT_FAIL, jsonError.toString());
                }
            }
        });
    }

    private JSONObject bindWrapper(OAuthData data) throws Exception {
        JSONObject jsonResult = new JSONObject();
        int resultCode = data.getResultCode();
        switch (resultCode) {
            case OAuthData.RESULT_CODE_SUCCESS:
                jsonResult.put("result", IChannelSDKServicePlugin.USER_BIND_RESULT_SUCESS);
                jsonResult.put("uid", data.getUid());
                jsonResult.put("msg", data.getErrorDescription());
                break;
            case OAuthData.RESULT_CODE_CANCEL:
                jsonResult.put("result", IChannelSDKServicePlugin.USER_BIND_RESULT_CANCEL);
                jsonResult.put("msg", data.getErrorDescription());
                break;
            case OAuthData.RESULT_CODE_FAIL:
                jsonResult.put("result", IChannelSDKServicePlugin.USER_BIND_RESULT_FAILED);
                jsonResult.put("msg", data.getErrorDescription());
                break;
        }
        return jsonResult;
    }

    private void register(final IChannelSDKCallback callback) {
        GplayPaySDK.register(mActivity, new GplayPaySDK.OAuthResponse() {
            @Override
            public void onResponse(OAuthData data) {
                try {
                    JSONObject jsonObject = registerWrapper(data);
                    callback.onCallback(jsonObject.optInt("result"), jsonObject.toString());
                } catch (Exception e) {
                    JSONObject jsonError = createUnexceptionJSON(e);
                    callback.onCallback(IChannelSDKServicePlugin.USER_REGISTER_RESULT_FAIL, jsonError.toString());
                }
            }
        });
    }

    private JSONObject payWrapper(PayData data) throws Exception {
        JSONObject jsonResult = new JSONObject();
        int resultCode = data.getResultCode();
        switch (resultCode){
            case PayData.RESULT_CODE_SUCCESS:
                jsonResult.put("result", IChannelSDKServicePlugin.PAY_RESULT_SUCCESS);
                jsonResult.put("msg", data.getErrorDescription());
                jsonResult.put("pay_real_amount", data.getAmount());
                break;
            case PayData.RESULT_CODE_FAIL:
                jsonResult.put("result", IChannelSDKServicePlugin.PAY_RESULT_FAIL);
                jsonResult.put("msg", data.getErrorDescription());
                break;
            case PayData.RESULT_CODE_CANCEL:
                jsonResult.put("result", IChannelSDKServicePlugin.PAY_RESULT_CANCEL);
                jsonResult.put("msg", data.getErrorDescription());
                break;
            case PayData.RESULT_CODE_INVALID:
                jsonResult.put("result", IChannelSDKServicePlugin.PAY_RESULT_INVALID);
                jsonResult.put("msg", data.getErrorDescription());
                break;
        }
        return jsonResult;
    }

    private JSONObject loginWrapper(OAuthData data) throws Exception{
        JSONObject jsonResult = new JSONObject();
        int resultCode = data.getResultCode();
        switch (resultCode){
            case OAuthData.RESULT_CODE_SUCCESS:
                jsonResult.put("result", IChannelSDKServicePlugin.USER_LOGIN_RESULT_SUCCESS);
                jsonResult.put("msg", data.getErrorDescription());

                //返回服务端二次认证需要的数据
                // 注: 这部分内容必须按照渠道服务端规范来设置
                //========================== begin ===================//

                JSONObject jsonData = new JSONObject();

                //Gplay 第三方SDK服务端数据规范要求传递键为"access_token"的token值
                jsonData.put("access_token", GplayPaySDK.getAccessToken());

                //======================== end ======================//

                jsonResult.put("data", jsonData);
                break;
            case OAuthData.RESULT_CODE_CANCEL:
                jsonResult.put("result", IChannelSDKServicePlugin.USER_LOGIN_RESULT_CANCEL);
                jsonResult.put("msg", data.getErrorDescription());
                break;
            case OAuthData.RESULT_CODE_FAIL:
                jsonResult.put("result", IChannelSDKServicePlugin.USER_LOGIN_RESULT_FAIL);
                jsonResult.put("msg", data.getErrorDescription());
                break;
        }
        return jsonResult;
    }

    private JSONObject registerWrapper(OAuthData data) throws Exception{
        JSONObject jsonResult = new JSONObject();
        int resultCode = data.getResultCode();
        switch (resultCode){
            case OAuthData.RESULT_CODE_SUCCESS:
                jsonResult.put("result", IChannelSDKServicePlugin.USER_REGISTER_RESULT_SUCCESS);
                jsonResult.put("uid", data.getUid());
                jsonResult.put("msg", data.getErrorDescription());
                break;
            case OAuthData.RESULT_CODE_CANCEL:
                jsonResult.put("result", IChannelSDKServicePlugin.USER_REGISTER_RESULT_CANCEL);
                jsonResult.put("msg", data.getErrorDescription());
                break;
            case OAuthData.RESULT_CODE_FAIL:
                jsonResult.put("result", IChannelSDKServicePlugin.USER_REGISTER_RESULT_FAIL);
                jsonResult.put("msg", data.getErrorDescription());
                break;
        }
        return jsonResult;
    }

    private JSONObject createUnexceptionJSON(Exception e) {
        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("result", IChannelSDKServicePlugin.USER_LOGIN_RESULT_FAIL);
            jsonResult.put("msg", e.getMessage());
            return jsonResult;
        } catch(Exception ex) {
            ex.printStackTrace();;
        }
        return jsonResult;
    }

    private JSONObject createUnsupportResponse(String method) {
        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("result", IChannelSDKServicePlugin.RESULT_NOT_SUPPORT_FUNCTION);
            jsonResult.put("msg", method + " not support!");
            return jsonResult;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return jsonResult;
    }

    private void createShortcut(String appName, String iconFile, String appid, String orientation, String engineType, String engineVersion, IChannelSDKCallback callback) {
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
        shortcut.putExtra("duplicate", false);//设置是否重复创建
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        //666666 改成接入APP分配的渠道号
        Uri uri = Uri.parse("gplay://gplay_channel_666666");
        intent.setData(uri);
        intent.putExtra("client_id", appid);
        intent.putExtra("orientation", orientation);
        //设置打开游戏Activity的类
        intent.setClass(mActivity, mActivity.getClass());
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        int size = (int) mActivity.getResources().getDimension(android.R.dimen.app_icon_size);
        Bitmap icon = BitmapFactory.decodeFile(iconFile);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, Bitmap.createScaledBitmap(icon, size, size, false));
        mActivity.sendBroadcast(shortcut);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("result", IChannelSDKServicePlugin.SHORTCUT_RESULT_SUCCESS);
            jsonObject.put("msg", "");
            callback.onCallback(IChannelSDKServicePlugin.SHORTCUT_RESULT_SUCCESS, jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {

    }

    private void downloadFile(final String downloadUrl, final String savePath, final OnFileDownloadListener listener) {
        new Thread() {
            public void run() {
                Log.d(TAG, "downloadFile... " + downloadUrl);
                HttpURLConnection httpConn = null;
                try {
                    URL url = new URL(downloadUrl);
                    httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setConnectTimeout(5000);
                    httpConn.setRequestMethod("GET");
                } catch (Exception e) {
                    Log.e(TAG, "downloadFile failed(open connection exception)!");
                    listener.onFailure();
                    return;
                }

                File file = new File(savePath);
                if (file.exists()) {
                    file.delete();
                }
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Log.e(TAG, "downloadFile failed(create file exception)!");
                    listener.onFailure();
                    return;
                }
                int fileLength = 0;
                int downedFileLength = 0;
                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    inputStream = httpConn.getInputStream();
                } catch (Exception e) {
                    Log.e(TAG, "downloadFile failed(get input stream exception)!");
                    listener.onFailure();
                    file.delete();
                    return;
                }
                try {
                    byte[] buffer = new byte[1024 * 4];
                    fileLength = httpConn.getContentLength();

                    if (fileLength > 0) {
                        Log.d(TAG, "download file start");
                        listener.onStart();
                    }
                    int size = -1;
                    while ((size = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, size);
                        downedFileLength += size;
                        listener.onProgress(downedFileLength, fileLength);
                    }
                    Log.d(TAG, "download file success");
                    listener.onSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    httpConn.disconnect();
                    tryClose(outputStream);
                    tryClose(inputStream);
                }
            }
        }.start();


    }

    private void tryClose(InputStream is) {
        if(null != is) {
            try {
                is.close();
                is = null;
            } catch( Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void tryClose(OutputStream os) {
        if(null != os) {
            try {
                os.close();
                os = null;
            } catch( Exception e) {
                e.printStackTrace();
            }
        }
    }
}
