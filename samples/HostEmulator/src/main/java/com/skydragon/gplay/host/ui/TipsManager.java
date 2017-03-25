package com.skydragon.gplay.host.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.skydragon.gplay.Gplay;
import com.skydragon.gplay.IGplayService;

/**
 * 启动游戏过程中的提示框
 * 注: 建议接入APP定制此提示框
 */
public class TipsManager {

    public static final int MODE_TYPE_GAME_BACK = 0;
    public static final int MODE_TYPE_LOADING_BACK = 1;
    public static final int MODE_TYPE_NETWORK_FAILED = 2;
    public static final int MODE_TYPE_INCOMPATIBLE = 3;
    public static final int MODE_TYPE_MAINTAINING = 4;
    public static final int MODE_TYPE_INVISIBLE = 5;
    public static final int MODE_TYPE_GAME_NOT_EXIST = 6;
    public static final int MODE_TYPE_FILE_VERIFY_WRONG = 7;
    public static final int MODE_TYPE_ARCH_NOT_SUPPORTED = 8;

    private int mode;

    private Activity mActivity;
    private IGplayService mGplayService;
    private String mClientId;

    private AlertDialog.Builder builder;
    private AlertDialog mDialog;

    public TipsManager(Activity activity, IGplayService gplayService) {
        mActivity = activity;
        mGplayService = gplayService;
        initDialog();
    }

    private void initDialog() {
        builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (mode) {
                case MODE_TYPE_NETWORK_FAILED:
                case MODE_TYPE_FILE_VERIFY_WRONG:
                    if (mGplayService != null) {
                        mGplayService.cancelStartGame();
                        mGplayService.retryStartGame(mClientId);
                        dialog.dismiss();
                    }
                    break;
                case MODE_TYPE_GAME_BACK:
                    if (mGplayService != null) {
                        mGplayService.closeGame();
                    }
                    dialog.dismiss();
                    mActivity.finish();
                    break;
                case MODE_TYPE_LOADING_BACK:
                    if (mGplayService != null) {
                        mGplayService.cancelStartGame();
                    }
                    dialog.dismiss();
                    mActivity.finish();
                    break;
                case MODE_TYPE_GAME_NOT_EXIST:
                case MODE_TYPE_INCOMPATIBLE:
                case MODE_TYPE_MAINTAINING:
                case MODE_TYPE_INVISIBLE:
                case MODE_TYPE_ARCH_NOT_SUPPORTED:
                    if (mGplayService != null) {
                        mGplayService.cancelStartGame();
                    }
                    dialog.dismiss();
                    mActivity.finish();
                    break;
                }

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (mode) {
                case MODE_TYPE_GAME_BACK:
                case MODE_TYPE_LOADING_BACK:
                    dialog.dismiss();
                    break;
                case MODE_TYPE_NETWORK_FAILED:
                case MODE_TYPE_GAME_NOT_EXIST:
                case MODE_TYPE_INCOMPATIBLE:
                case MODE_TYPE_MAINTAINING:
                case MODE_TYPE_INVISIBLE:
                case MODE_TYPE_FILE_VERIFY_WRONG:
                case MODE_TYPE_ARCH_NOT_SUPPORTED:
                    if (mGplayService != null) {
                        mGplayService.cancelStartGame();
                    }
                    dialog.dismiss();
                    mActivity.finish();
                    break;
                }
            }
        });
        mDialog = builder.create();
    }
    
    public void setClientId(String clientId) {
        mClientId = clientId;
    }

    public int getMode(String type) {
        if (type.equals(Gplay.DOWNLOAD_ERROR_NETWORK_FAILED)) {
            return MODE_TYPE_NETWORK_FAILED;
        }
        if (type.equals(Gplay.DOWNLOAD_ERROR_INCOMPATIBLE)) {
            return MODE_TYPE_INCOMPATIBLE;
        }
        if (type.equals(Gplay.DOWNLOAD_ERROR_MAINTAINING)) {
            return MODE_TYPE_MAINTAINING;
        }
        if (type.equals(Gplay.DOWNLOAD_ERROR_INVISIBLE)) {
            return MODE_TYPE_INVISIBLE;
        }
        if (type.equals(Gplay.DOWNLOAD_ERROR_GAME_NOT_EXIST)) {
            return MODE_TYPE_GAME_NOT_EXIST;
        }
        if (type.equals(Gplay.DOWNLOAD_ERROR_FILE_VERIFY_WRONG)) {
            return MODE_TYPE_FILE_VERIFY_WRONG;
        }
        if (type.equals(Gplay.DOWNLOAD_ERROR_ARCH_NOT_SUPPORTED)) {
            return MODE_TYPE_ARCH_NOT_SUPPORTED;
        }
        return MODE_TYPE_NETWORK_FAILED;
    }

    public void show(String type) {
        mode = getMode(type);
        show(mode);
    }
    
    public void show(int type) {
        mode = type;
        switch (mode) {
        case MODE_TYPE_LOADING_BACK:
            builder.setMessage("确定退出加载吗?");
            break;
        case MODE_TYPE_GAME_BACK:
            builder.setMessage("确定退出游戏吗?");
            break;
        case MODE_TYPE_NETWORK_FAILED:
            builder.setMessage("连接异常,请检查网络设置后重试.");
            break;
        case MODE_TYPE_INCOMPATIBLE:
            builder.setMessage("不兼容,您的版本过低,请升级应用");
            break;
        case MODE_TYPE_MAINTAINING:
            builder.setMessage("游戏正在维护中,请稍候.");
            break;
        case MODE_TYPE_INVISIBLE:
            builder.setMessage("游戏不可见,获取游戏信息失败!");
            break;
        case MODE_TYPE_GAME_NOT_EXIST:
            builder.setMessage("糟糕，游戏资源没加载到，再试试吧！");
            break;
        case MODE_TYPE_FILE_VERIFY_WRONG:
            builder.setMessage("文件校验失败！");
            break;
        case MODE_TYPE_ARCH_NOT_SUPPORTED:
            builder.setMessage("很抱歉，游戏暂不支持该设备！");
        }
        mDialog = builder.create();
        mDialog.show();
    }

}
