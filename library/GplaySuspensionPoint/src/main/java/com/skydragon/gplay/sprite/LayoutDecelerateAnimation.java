package com.skydragon.gplay.sprite;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class LayoutDecelerateAnimation implements LayoutAnimation {
    private static final String TAG = "LayoutDecelerateAnimation";
    /**
     * 每秒画50帧
     */
    private static final int DRAW_TIME_ON_ONE_FRAME = 20;
    private OnLayoutAnimationListener mAnimationListener;
    private View mDragonView;
    private boolean bAnimationEnd;
    private int mCurrentFrame;
    private int mTotalFrame;
    private int mFromX;
    private int mFromY;
    private int mXDistance;
    private int mYDistance;
    private int mImageSize;
    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    public void start(View view, int fromX, int fromY, int toX, int toY, int size,  int duration) {
        mDragonView = view;
        mCurrentFrame = 0;
        mFromX = fromX;
        mFromY = fromY;
        mImageSize = size;
        int totalFrames = (int)duration / DRAW_TIME_ON_ONE_FRAME;
        mTotalFrame = totalFrames;
        mXDistance = toX - fromX;
        mYDistance = toY - fromY;
        bAnimationEnd = false;
        if(view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        animationTask.run();
    }

    @Override
    public void setAnimationListener(OnLayoutAnimationListener listener) {
        mAnimationListener = listener;
    }

    @Override
    public boolean isAnimationEnd() {
        return bAnimationEnd;
    }

    private int decelerate(int currentFrame, int totalFrame, int initValue, int disatance) {
        double t = (currentFrame * 1.0d ) / totalFrame;
        t--;
        return (int)(disatance * Math.sqrt(1 - t*t) + initValue);
    }

    Runnable animationTask = new Runnable() {
        @Override
        public void run() {
            if(mCurrentFrame == 0) {
                if(null != mAnimationListener) {
                    mAnimationListener.onAnimationStart();
                }
            }
            if(mCurrentFrame > mTotalFrame ) {
                if(null != mAnimationListener) {
                    mAnimationListener.onAnimationEnd();
                }
                bAnimationEnd = true;
                return;
            }
            mCurrentFrame++;
            int curX = decelerate(mCurrentFrame, mTotalFrame, mFromX, mXDistance);
            int curY = decelerate(mCurrentFrame, mTotalFrame, mFromY, mYDistance);
            mDragonView.layout(curX, curY, curX + mImageSize, curY + mImageSize);
            mDragonView.postInvalidate();
            if(null != mAnimationListener) {
                mAnimationListener.onAnimation(curX, curY);
            }


            mUiHandler.postDelayed(this, 20);
        }
    };
}
