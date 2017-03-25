package com.skydragon.gplay;


import android.os.Environment;

public final class Constants {
    /**
     * Gplay Demo Id, 接入APP需要配置自己渠道的ID,这个ID由Gplay进行分配
     */
    public static final String DEFAULT_CHANNEL_ID = "666666";

    //游戏资源文件存放目录
    public static final String DEFAULT_CACHE_DIR = Environment.getExternalStorageDirectory() + "/gplay/";

    public static final String KEY_CHANNEL_ID = "gplay_channel_id";

    public static final String KEY_CACHE_DIR = "gplay_cache_dir";

    public static final String KEY_RUNTIME_DIR = "gplay_runtime_dir";

    public static final String KEY_PRODUCT_MODE = "gplay_product_mode";

    public static final String URL_SHARE_HTML = "http://skydragon-inc.cn/demo.html?client_id=%s&orientation=%s";
}
