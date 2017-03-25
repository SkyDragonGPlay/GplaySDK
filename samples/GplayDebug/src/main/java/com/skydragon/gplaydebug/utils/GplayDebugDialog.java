package com.skydragon.gplaydebug.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.skydragon.gplaydebug.R;

/**
 * package : com.skydragon.gplaydebug.view
 * <p/>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.14 18:10.
 */
public class GplayDebugDialog {
    public static final int BTN_POSITIVE_CLICK = 0;
    public static final int BTN_NEGATIVE_CLICK = 1;

    private static AlertDialog.Builder buildDialog(Context context, String title, String message
            , String positiveTxt, String negativeTxt, final OnDialogClickListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        if(message != null)
            builder.setMessage(message);

        if(positiveTxt == null)
            positiveTxt = context.getString(R.string.dialog_confirm);

        builder.setPositiveButton(positiveTxt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(listener != null)
                    listener.onDialogButtonClick(BTN_POSITIVE_CLICK, dialog, which);
            }
        });

        if(negativeTxt != null){
            builder.setNegativeButton(negativeTxt, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(listener != null)
                        listener.onDialogButtonClick(BTN_NEGATIVE_CLICK, dialog, which);
                }
            });
        }

        return builder;
    }

    public static void showDialog(Context context, String title, String msg, String positiveBtnTxt, OnDialogClickListener listener) {
        AlertDialog.Builder builder = buildDialog(context, title, msg, positiveBtnTxt, null, listener);
        builder.create().show();
    }

    public static void showDialog(Context context, String title, String msg, String positiveBtnTxt, String negativeBtnTxt, OnDialogClickListener listener) {
        AlertDialog.Builder builder = buildDialog(context, title, msg, positiveBtnTxt, negativeBtnTxt, listener);
        builder.create().show();
    }

    public interface OnDialogClickListener{
        public void onDialogButtonClick(int btnMode, DialogInterface dialog, int which);
    }
}
