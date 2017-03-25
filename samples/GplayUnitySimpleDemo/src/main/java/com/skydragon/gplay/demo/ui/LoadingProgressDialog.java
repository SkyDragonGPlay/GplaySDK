package com.skydragon.gplay.demo.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.skydragon.gplay.demo.R;
import com.zhy.view.RoundProgressBarWidthNumber;


/**
 * package : com.skydragon.hybridsdk.view
 *
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016/4/15 10:40.
 */

public class LoadingProgressDialog extends Dialog{
    private static TimeoutListener mTimeoutListener;
    private static LoadingProgressDialog mLoadingDialog;
    private CharSequence mMsg;
    private LayoutInflater layoutInflater;
    private RoundProgressBarWidthNumber mProgressBar;

    private LoadingProgressDialog(Context context, CharSequence message) {
        super(context, R.style.LoadingDialogStyle);

        mMsg = message;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.layout_loading_progress_dialog, null);

        TextView msgTextView = (TextView) view.findViewById(R.id.loading_dialog_message);
        mProgressBar = (RoundProgressBarWidthNumber) view.findViewById(R.id.loading_dialog_progress);
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
        mLoadingDialog = new LoadingProgressDialog(context, message);
        mLoadingDialog.show();
    }

    public static void onProgress(long already, long total){
        float percent = 0;
        if(already > 0)
            percent = ((float)already * 100) / total;
        onProgress(percent);
    }


    public static void onProgress(float percent){
        if(mLoadingDialog != null && mLoadingDialog.mProgressBar != null) {
            mLoadingDialog.mProgressBar.setProgress((int)percent);
        }
    }

    public static void closeLoadingDialog() {
        Log.v("", "closeLoadingDialog " + mLoadingDialog);
        if(mLoadingDialog != null){
            try{
                Log.v("", "closeLoadingDialog dismiss" );
                mLoadingDialog.dismiss();
            } catch (Exception e){
                Log.e("", e.toString());
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
