package com.skydragon.gplay.sprite;

import android.view.View;

public interface LayoutAnimation {
    public void start(View view, int fromX, int fromY, int toX, int toY, int size, int duration);
    public void setAnimationListener(OnLayoutAnimationListener listener);
    public boolean isAnimationEnd();

    interface OnLayoutAnimationListener {
        void onAnimationStart();
        void onAnimation(int curX, int curY);
        void onAnimationEnd();
    }
}
