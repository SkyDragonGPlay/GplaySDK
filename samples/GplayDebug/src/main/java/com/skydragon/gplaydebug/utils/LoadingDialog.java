package com.skydragon.gplaydebug.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.skydragon.gplaydebug.R;


/**
 * package : com.skydragon.hybridsdk.view
 *
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016/4/15 10:40.
 */

public class LoadingDialog extends Dialog{
    private static Handler mPostDelateHandler = new Handler(){};
    private static TimeoutListener mTimeoutListener;
    private static LoadingDialog mLoadingDialog;
    private CharSequence mMsg;
    private LayoutInflater layoutInflater;

    private LoadingDialog(Context context, CharSequence message) {
        super(context, R.style.LoadingDialogStyle);

        mMsg = message;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.layout_loading_dialog, null);

        TextView msgTextView = (TextView) view.findViewById(R.id.loading_dialog_message);
        if (msgTextView != null && !TextUtils.isEmpty(mMsg))
            msgTextView.setText(mMsg);
        setCancelable(false);
        setContentView(view);
        super.onCreate(savedInstanceState);
    }

    public static void showLoadingDialog(Context context, CharSequence message){
        if(context == null){
            return;
        }

        if((context instanceof Activity)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if(((Activity) context).isDestroyed() || ((Activity) context).isFinishing())
                    return;
            } else {
                if(((Activity) context).isFinishing())
                    return;
            }
        }

        closeLoadingDialog();
        mLoadingDialog = new LoadingDialog(context, message);
        mLoadingDialog.show();
    }

    public static void showLoadingDialog(Context context, CharSequence message, int showTimes, TimeoutListener listener){
        mTimeoutListener = listener;

        showLoadingDialog(context, message);

        mPostDelateHandler.postDelayed(mClosePDThread, showTimes);
    }

    public static void closeLoadingDialog() {
        mPostDelateHandler.removeCallbacks(mClosePDThread);
        if(mLoadingDialog != null){

            try{
                mLoadingDialog.dismiss();
            } catch (Exception e){
                Log.v("", e.toString());
            }
            mLoadingDialog = null;
        }
    }

    private static Runnable mClosePDThread = new Runnable() {
        @Override
        public void run() {
            closeLoadingDialog();

            if(mTimeoutListener != null){
                mTimeoutListener.onTimeout();
                mTimeoutListener = null;
            }
        }
    };

    public interface TimeoutListener{
        public void onTimeout();
    }
}
