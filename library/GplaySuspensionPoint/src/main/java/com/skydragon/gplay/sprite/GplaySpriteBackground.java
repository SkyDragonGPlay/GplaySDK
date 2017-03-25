package com.skydragon.gplay.sprite;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;


public class GplaySpriteBackground extends FrameLayout implements View.OnTouchListener, GplaySpritePoint.OnExpandAndCollapseListener{

    private boolean isExpanded;
    private OnSuspensionBackgroundClickListener mListener;
    private int mTouchSlopSquare;

    private Drawable mExpandDrawable;

    public GplaySpriteBackground(Context context) {
        super(context);
        init();
    }

    public void setExpandBackground(Drawable d) {
        mExpandDrawable = d;
    }

    private void init() {
        this.setOnTouchListener(this);
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlopSquare = configuration.getScaledTouchSlop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.setBackgroundResource(android.R.color.transparent);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(!changed) return;
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onCollapse() {
        isExpanded = false;
        this.setBackgroundResource(android.R.color.transparent);
    }

    @Override
    public void onExpand() {
        isExpanded = true;
        if(null != mExpandDrawable) {
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
                this.setBackground(mExpandDrawable);
            } else {
                this.setBackgroundDrawable(mExpandDrawable);
            }
        }
    }


    private int mStartX;
    private int mStartY;
    private boolean mIsClickEvent = true;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(!isExpanded) return false;

        int action = event.getAction();

        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (action) {
            // 鼠标按下 拖拉动作开始
            case MotionEvent.ACTION_DOWN:
                mStartX = x;
                mStartY = y;
                mIsClickEvent = true;
                break;

            // 鼠标移动 拖拉动作进行中
            case MotionEvent.ACTION_MOVE:
                if( mIsClickEvent ) {
                    if(Math.abs(x - mStartX) > mTouchSlopSquare || Math.abs(y - mStartY) > mTouchSlopSquare) {
                        mIsClickEvent = false;
                    }
                }
                break;
            // 鼠标释放 拖拉动作结束
            case MotionEvent.ACTION_UP:
                if(mIsClickEvent) {
                    if( null != mListener ) {
                        mListener.onSuspensionBackGroundClick();
                    }
                }
                break;
        }
        return isExpanded;
    }


    interface OnSuspensionBackgroundClickListener {
        void onSuspensionBackGroundClick();
    }

    public void setOnSuspensionBackgroundClickListener(OnSuspensionBackgroundClickListener listener) {
        mListener = listener;
    }


}
