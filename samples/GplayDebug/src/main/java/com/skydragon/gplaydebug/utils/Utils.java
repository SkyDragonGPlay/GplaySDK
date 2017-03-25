package com.skydragon.gplaydebug.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;

/**
 * package : com.skydragon.gplaydebug.utils
 * <p/>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.14 16:42.
 */
public class Utils {

    private static String sPreferencesSuffix = "_preferences";
    public static SharedPreferences getSettingSharedPreferences(Context ctx) {
        String preferencesName = ctx.getPackageName() + sPreferencesSuffix;
        SharedPreferences sh = ctx.getSharedPreferences(preferencesName, Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
        return sh;
    }

    /**
     * Convert Dp to Pixel
     */
    public static int dpToPx(float dp, Resources resources){
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static int getRelativeTop(View myView) {
        if(myView.getId() == android.R.id.content)
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }

    public static int getRelativeLeft(View myView) {
        if(myView.getId() == android.R.id.content)
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }
}
