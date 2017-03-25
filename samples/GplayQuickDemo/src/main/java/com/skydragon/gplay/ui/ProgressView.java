package com.skydragon.gplay.ui;

import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 启动游戏准备运行数据的进度条
 * 注: 建议渠道定制此进度条
 */
public class ProgressView extends RelativeLayout {

    private Context mContext;

    private String mCurrentTip = "资源加载中...";

    private TextView tv;

    public ProgressView(Context context) {
        super(context);
        mContext = context;
        tv = new TextView(mContext);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.bottomMargin = 60;
        tv.setLayoutParams(layoutParams);
        tv.setText(mCurrentTip);
        tv.setTextColor(Color.BLACK);
        this.addView(tv);
    }

    public void updateProgress(int downloadedPercent, int downloadSpeed) {
        if (downloadSpeed == -1) {
            return;
        }

        String speedText = String.format("%.2f", downloadSpeed * 1.0f / 1024) + "KB";
        tv.setText(mCurrentTip + downloadedPercent + "%" + " " + speedText);
    }

}
