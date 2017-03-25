package com.skydragon.gplay.demo;


public final class Constants {
    /**
     * Gplay Demo Id, 接入APP需要配置自己渠道的ID,这个ID由Gplay进行分配
     */
    public static final String DEFAULT_CHANNEL_ID = "666666";

    public static final String KEY_CHANNEL_ID = "gplay_channel_id";

    public static final String KEY_CACHE_DIR = "gplay_cache_dir";

    public static final String KEY_RUNTIME_HOST_URL = "gplay_runtime_host_url";

    public static final int USE_GPLAY_H5_PAYSDK  = 0;
    public static final int USE_GPLAY_PAYSDK  = 1;

    public static final int ONE  = 1;
    public static final int ZERO  = 0;

    public static final String URL_SHARE_HTML = "http://skydragon-inc.cn/demo.html?client_id=%s&orientation=%s";
}