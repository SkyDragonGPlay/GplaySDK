/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yolanda.nohttp.tools;

import android.net.ConnectivityManager;

/**
 * <p>
 * Check the network utility class.
 * </p>
 * Created in Jul 31, 2015 1:19:47 PM.
 *
 * @author Yan Zhenjie.
 */
public class NetUtil {

    private static int CURR_NETWORK_TYPE = -1;

    public static void setCurrNetworkType(int networkType) {
        CURR_NETWORK_TYPE = networkType;
    }

    public static int getCurrNetworkType() {
        return CURR_NETWORK_TYPE;
    }

    /**
     * Check the network is enable.
     *
     * @return Available returns true, unavailable returns false.
     */
    public static boolean isNetworkAvailable() {
        return CURR_NETWORK_TYPE != -1;
    }

    /**
     * To determine whether a WiFi network is available.
     *
     * @return Open return true, close returns false.
     */
    public static boolean isWifiConnected() {
        return CURR_NETWORK_TYPE == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * To determine whether a mobile phone network is available.
     *
     * @return Open return true, close returns false.
     */
    public static boolean isMobileConnected() {
        return CURR_NETWORK_TYPE == ConnectivityManager.TYPE_MOBILE;
    }
}
