package com.skydragon.gplaydebug;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * package : com.skydragon.gplaydebug
 * <p/>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.7 14:19.
 */
public class GpalyDebugApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
