package com.skydragon.gplay;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.skydragon.gplay.callback.OnDownloadListener;
import com.skydragon.gplay.callback.OnPrepareRuntimeListener;
import com.skydragon.gplay.callback.OnRequestGameInfoListListener;
import com.skydragon.gplay.constants.FileConstants;
import com.skydragon.gplay.loader.DexLoader;
import com.skydragon.gplay.service.IDownloadProxy;
import com.skydragon.gplay.service.IRuntimeBridge;
import com.skydragon.gplay.thirdsdk.IChannelSDKBridge;
import com.skydragon.gplay.thirdsdk.IChannelSDKServicePlugin;
import com.skydragon.gplay.utils.FileUtils;
import com.skydragon.gplay.utils.HttpUtils;
import com.skydragon.gplay.utils.RuntimeSDKDownloader;

import java.io.File;
import java.util.HashMap;

/**
 * SDK主类
 */
public final class Gplay {

    private static final String VERSION = "1.2.1";
    private static final int VERSION_CODE = 8;

    public static final String DOWNLOAD_ERROR_GAME_NOT_EXIST = "error_game_not_exist";
    public static final String DOWNLOAD_ERROR_NETWORK_FAILED = "error_network_failed";
    public static final String DOWNLOAD_ERROR_INVISIBLE = "error_invisible";
    public static final String DOWNLOAD_ERROR_INCOMPATIBLE = "error_incompatible";
    public static final String DOWNLOAD_ERROR_MAINTAINING = "error_maintaining";
    public static final String DOWNLOAD_ERROR_ARCH_NOT_SUPPORTED = "error_arch_not_supported";
    public static final String DOWNLOAD_ERROR_FILE_VERIFY_WRONG = "error_file_verify_wrong";

    public static final String ONLINE_MODE = "online";
    public static final String SANDBOX_MODE = "sandbox";
    public static final String DEVELOP_MODE = "develop";

    static final String ONLINE_SERVER = "http://open.api.skydragon-inc.cn/";
    static final String SANDBOX_SERVER = "http://sandbox.api.skydragon-inc.cn/";
    static final String DEVELOP_SERVER = "http://dev.api.skydragon-inc.cn/";

    public static final String REQUEST_GAME_LIST_URL =  "api/game/list?chn=%s";

    private static RuntimeSDKDownloader sDownloader;
    private static boolean isSDKPrepared;
    private static IGplayService sGplayService;

    private static boolean isPreDownloadRuntimeSDK;

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    private static String PRODUCT_MODE = SANDBOX_MODE;

    private static HashMap<String,GplayServerInfo> serverInfos = new HashMap<>();

    private static IChannelSDKBridge sChannelSDKBridge;

    static {
        initServerInfo();
    }

    private Gplay() {}

    public static void prepareRuntime(final Context context, final String channelID, final String cacheDir, final IChannelSDKServicePlugin channelSDKProxy, final OnPrepareRuntimeListener listener ) {
        prepareRuntime(context, channelID, getGplayRuntimeDefaultDir(context), cacheDir, channelSDKProxy, listener);
    }

    public static void prepareRuntime(final Context context, final String channelID, final String runtimeDir, final String cacheDir, final IChannelSDKServicePlugin channelSDKServicePlugin, final OnPrepareRuntimeListener listener) {
        prepareRuntime(context, channelID, runtimeDir, cacheDir, channelSDKServicePlugin, listener, false);
    }

    public static void prepareRuntime(final Context context, final String channelID, final String runtimeDir, final String cacheDir, final IChannelSDKServicePlugin channelSDKServicePlugin, final OnPrepareRuntimeListener listener, boolean isDebug) {
        isPreDownloadRuntimeSDK = false;
        if(isSDKPrepared) {
            listener.onPrepareRuntimeSuccess(sGplayService);
            return;
        }

        FileConstants.init(context);

        if(isDebug) {
            IRuntimeBridge runtimeBridge = DexLoader.loadRuntimeBridgeImpl();
            if (null != runtimeBridge) {
                GplayServiceImpl serviceImpl = new GplayServiceImpl(runtimeBridge);
                sGplayService = serviceImpl;
                isSDKPrepared = true;
                serviceImpl.init(context, channelID, runtimeDir, cacheDir, sChannelSDKBridge, channelSDKServicePlugin, listener);

            } else {
                FileUtils.removeFile(FileConstants.getLatestGplaySDKPath());
                listener.onPrepareRuntimeFailure("load failed");
            }
            return;
        }

        int currNetworkType = -1;

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null) {
            currNetworkType = networkInfo.getType();
        }

        // 假如 SDK 未就绪, 下载并加载 runtimeService,
        // 初始化 GplayService, 设置渠道 SDK 代理,
        // 调用 Runtime 已准备就绪的回调
        final OnDownloadListener listener1 = new OnDownloadListener() {
            @Override
            public void onStart() {
                if (null != listener) {
                    listener.onPrepareRuntimeStart();
                }
            }

            @Override
            public void onProgress(long downloadedSize, long totalSize) {
                if (null != listener) {
                    listener.onPrepareRuntimeProgress(downloadedSize, totalSize);
                }
            }

            @Override
            public void onSuccess() {
                if (null != listener) {
                    IRuntimeBridge runtimeBridge = DexLoader.loadRuntimeBridgeImpl();
                    if (null != runtimeBridge) {
                        GplayServiceImpl serviceImpl = new GplayServiceImpl(runtimeBridge);
                        sGplayService = serviceImpl;
                        isSDKPrepared = true;
                        serviceImpl.init(context, channelID, runtimeDir, cacheDir, sChannelSDKBridge, channelSDKServicePlugin, listener);
                    } else {
                        FileUtils.removeFile(FileConstants.getLatestGplaySDKPath());
                        listener.onPrepareRuntimeFailure("load failed");
                    }
                }
            }

            @Override
            public void onFailure(String msg) {
                if (null != listener) {
                    listener.onPrepareRuntimeFailure(msg);
                }
                isSDKPrepared = false;
            }

            @Override
            public void onCancel() {
                if (null != listener) {
                    listener.onPrepareRuntimeCancel();
                }
                isSDKPrepared = false;
            }
        };

        if (currNetworkType != -1)
            doPrepareSDK(channelID, listener1);
        else {
            //无网络连接时,直接尝试加载本地runtime jar
            sHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listener1.onSuccess();
                }
            }, 10);
        }
    }
    public static void preDownloadRuntimeSDK(final Context context, final String channelID, final String runtimeDir, final String cacheDir, final OnDownloadListener listener) {
        FileConstants.init(context);
        isPreDownloadRuntimeSDK = true;
        prepareRuntime(context, channelID, runtimeDir, cacheDir, null, new OnPrepareRuntimeListener() {
            @Override
            public void onPrepareRuntimeStart() {
                listener.onStart();
            }

            @Override
            public void onPrepareRuntimeProgress(long downloadedSize, long totalSize) {

            }

            @Override
            public void onPrepareRuntimeSuccess(final IGplayService service) {
                sGplayService = service;
                GplayServiceImpl impl = (GplayServiceImpl) service;
                impl.setPrepareRuntimeProxy(new IDownloadProxy() {
                    @Override
                    public void onDownloadStart() {

                    }

                    @Override
                    public void onDownloadSuccess() {
                        listener.onSuccess();
                    }

                    @Override
                    public void onDownloadFailure(String error) {
                        listener.onFailure(error);
                    }
                });
                impl.prepareRuntime(context);
            }

            @Override
            public void onPrepareRuntimeFailure(String msg) {
                listener.onFailure(msg);
            }

            @Override
            public void onPrepareRuntimeCancel() {
                listener.onCancel();
            }
        });
    }

    /**
     * 设置产品模式, Gplay目前支持"线上","沙箱","开发"三种模式
     * 线上 Constants.ONLINE_MODE
     * 沙箱 Constants.SANDBOX_MODE
     * 开发 Constants.DEVELOP_MODE
     * @param mode 产品模式
     */
    public static void setProductMode(String mode) {
        PRODUCT_MODE = mode;
    }

    public static String getProductMode() {
        return PRODUCT_MODE;
    }

    public static void setChannelSDKBridge(IChannelSDKBridge channelSDKBridge) {
        sChannelSDKBridge = channelSDKBridge;
    }

    public static void cancelPreDownloadRuntimeSDK() {
        if(isPreDownloadRuntimeSDK && null != sGplayService ) {
            ((GplayServiceImpl)sGplayService).cancelPrepareRuntime();
        }
    }

    /**
     * 获取游戏列表
     * @param listener 回调
     */
    public static void requestGameList(final String channelId, final OnRequestGameInfoListListener listener) {
        String serverUrl = getServerUrl();
        String sGameListUrl = String.format(serverUrl + REQUEST_GAME_LIST_URL, channelId);

        HttpUtils.requestData(sGameListUrl, new HttpUtils.HttpResponseListener() {
            @Override
            public void onFailure() {
                sHandler.post(new Runnable() {
                    public void run() {
                        listener.onFailureOfRequestGames();
                    }
                });
            }

            @Override
            public void onSuccess(final String response) {
                sHandler.post(new Runnable() {
                    public void run() {
                        listener.onSuccessOfRequestGames(response);
                    }
                });
            }
        });
    }


    /** 尝试加载 RuntimeService 以查看 runtime 是否下载到本地并且可用
     * @return 是否能够加载 RuntimeService 类
     */
    public static boolean isGplayRuntimePrepared() {
        Object obj = DexLoader.loadRuntimeBridgeImpl();
        return null != obj;
    }

    /**
     * 取消下载SDK
     */
    public static void cancelPrepareRuntime() {
        if(null == sDownloader) return;
        sDownloader.cancelDownload();
    }

    /**
     * 获取服务Url
     * @return Game list URL
     */
    public static String getGameListUrl() {
        return getServerUrl() + "api/game/list";
    }

    public static String getSDKVersion() {
        return VERSION;
    }

    public static int getSDKVersionCode() {
        return VERSION_CODE;
    }

    /**
     * 准备所有插件（下载、更新）
     *
     * @param lis 准备过程的回调函数
     */
    private static void doPrepareSDK(final String channelId, final OnDownloadListener lis) {
        RuntimeSDKDownloader downloader = new RuntimeSDKDownloader(channelId);
        sDownloader = downloader;
        downloader.start(lis);
    }

    private static void initServerInfo() {
        GplayServerInfo onlineServerInfo = new GplayServerInfo();
        onlineServerInfo.tip = ONLINE_MODE;
        onlineServerInfo.runtimeServer = ONLINE_SERVER;
        onlineServerInfo.unitSDKServer = ONLINE_SERVER;
        serverInfos.put(ONLINE_MODE, onlineServerInfo);

        GplayServerInfo sandboxServerInfo = new GplayServerInfo();
        sandboxServerInfo.tip = SANDBOX_MODE;
        sandboxServerInfo.runtimeServer = SANDBOX_SERVER;
        sandboxServerInfo.unitSDKServer = SANDBOX_SERVER;
        serverInfos.put(SANDBOX_MODE, sandboxServerInfo);

        GplayServerInfo developServerInfo = new GplayServerInfo();
        developServerInfo.tip = DEVELOP_MODE;
        developServerInfo.runtimeServer = DEVELOP_SERVER;
        developServerInfo.unitSDKServer = DEVELOP_SERVER;
        serverInfos.put(DEVELOP_MODE, developServerInfo);
    }

    private static String getGplayRuntimeDefaultDir(Context context) {
        String ret = null;
        PackageManager pm = context.getPackageManager();

        try {
            ret = pm.getApplicationInfo(context.getPackageName(), 0).dataDir + "/";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(ret))
            return "";

        if (!ret.endsWith(File.separator)) {
            ret = ret + File.separator;
        }

        return ret + "gplay/";
    }

    static String getRuntimeServerUrl() {
        return serverInfos.get(PRODUCT_MODE).runtimeServer;
    }

    static String getUnitSDKServerUrl() {
        return serverInfos.get(PRODUCT_MODE).unitSDKServer;
    }

    public static String getServerUrl() {
        String serverUrl = ONLINE_SERVER;
        if(Gplay.ONLINE_MODE.equals(PRODUCT_MODE)) {
            serverUrl = ONLINE_SERVER;
        } else if(Gplay.SANDBOX_MODE.equals(PRODUCT_MODE)) {
            serverUrl = SANDBOX_SERVER;
        } else if(Gplay.DEVELOP_MODE.equals(PRODUCT_MODE)) {
            serverUrl = DEVELOP_SERVER;
        }
        return FileUtils.ensurePathEndsWithSlash(serverUrl);
    }
}
