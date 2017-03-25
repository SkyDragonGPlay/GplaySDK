package com.skydragon.gplay.sprite;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

public class GplaySpriteButton implements View.OnClickListener{
    private static final String TAG = "GplaySuspensionButton";
    private Drawable mIcon;
    private String mTitle;
    private int mButtonSize;
    private LayoutAnimation mLayoutExpandAnimation;
    private LayoutAnimation mLayoutShrinkAnimation;

    private OnGplaySuspensionButtonAnimationListener mSuspensionButtonAnimationListener;
    private OnSuspensionButtonClickListener mSuspensionButtonClickListener;

    private int mPosition;
    private int mDistance;
    private int mParentSize;
    private int mExpandDuration;
    private int mShrinkDuration;

    private int mStartPositionX;
    private int mStartPositionY;
    private int mEndPositionX;
    private int mEndPositionY;

    private SuspendButtonView mSuspendButtonView;

    private OnGplayButtonAction mGplayButtonAction;

    /**
     *
     * @param icon 图标
     * @param title 图标旁边显示的标题
     * @param viewSize 按钮大小
     */
    public GplaySpriteButton(Drawable icon, String title, int viewSize) {
        mIcon = icon;
        mButtonSize = viewSize;
        mTitle = title;
    }

    /**
     *
     * @param act
     * @param postion 位置
     * @param exPandDuration 动画时间
     */
    public void init(Activity act, int postion, int exPandDuration, int shrinkDuration) {
        mPosition = postion;
        mSuspendButtonView = new SuspendButtonView(act);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mSuspendButtonView.setLayoutParams(params);
        mLayoutExpandAnimation = new LayoutDecelerateAnimation();
        mLayoutShrinkAnimation = new LayoutDecelerateAnimation();
        mExpandDuration = exPandDuration;
        mShrinkDuration = shrinkDuration;
        mSuspendButtonView.setOnClickListener(this);
    }

    /**
     * 设置间隔距离
     * @param distance
     */
    void setSpacing(int distance) {
        mDistance = distance;
    }

    /**
     * 设置小白点大小
     * @param parentSize
     */
    void setParentSize(int parentSize) {
        mParentSize = parentSize;
    }

    View getView() {
        return mSuspendButtonView;
    }

    void setGplaySuspensionButtonAnimationListener(OnGplaySuspensionButtonAnimationListener listener) {
        mSuspensionButtonAnimationListener = listener;
    }

    void setGplaySuspensionButtonClickListener(OnSuspensionButtonClickListener listener) {
        mSuspensionButtonClickListener = listener;
    }

    /**
     * 设置展开移动动画
     * @param animation
     */
    public void setLayoutExpandAnimation(LayoutAnimation animation) {
        mLayoutExpandAnimation = animation;
    }

    /**
     * 设置收缩移动动画
     * @param animation
     */
    public void setLayoutShrinkAnimation(LayoutAnimation animation) {
        mLayoutShrinkAnimation = animation;
    }

    /**
     * 展开
     * @param direction 展开方向,0 向上, 1 向下
     * @param position 图标位置, 0左边, 1 右边
     */
    public void expand(int parentPositionX, int parentPositionY, int direction, int position) {
        int initPositionX = parentPositionX + (mParentSize - mButtonSize) / 2;
        if(position == GplaySpriteConstants.POSITION_LEFT) {
            initPositionX = parentPositionX + (mParentSize - mButtonSize) / 2;
        }
        int initPositionY = parentPositionY;
        if(direction == GplaySpriteConstants.DIRECTION_DOWN) {
            initPositionY = parentPositionY + mParentSize;
        }

        mSuspendButtonView.layout(initPositionX, initPositionY, initPositionX + mButtonSize, initPositionY + mButtonSize);

        int endPositionX = initPositionX;
        int endPositionY = initPositionY - mDistance * (mPosition+1) - mButtonSize * (mPosition+1);
        if(direction == GplaySpriteConstants.DIRECTION_DOWN) {
            endPositionY = initPositionY + mDistance * (mPosition+1) + mButtonSize * (mPosition);
        }

        mStartPositionX = initPositionX;
        mStartPositionY = initPositionY;
        mEndPositionX = endPositionX;
        mEndPositionY = endPositionY;
        if(null != mLayoutExpandAnimation) {
            mLayoutExpandAnimation.setAnimationListener(new LayoutAnimation.OnLayoutAnimationListener() {
                @Override
                public void onAnimationStart() {
                    if(null != mSuspensionButtonAnimationListener) {
                        mSuspensionButtonAnimationListener.onExpandAnimationStart(GplaySpriteButton.this);
                    }
                }

                @Override
                public void onAnimation(int curX, int curY) {
                    if(null != mSuspensionButtonAnimationListener) {
                        mSuspensionButtonAnimationListener.onExpandAnimation(GplaySpriteButton.this, curX, curY);
                    }
                }

                @Override
                public void onAnimationEnd() {
                    if(null != mSuspensionButtonAnimationListener) {
                        mSuspensionButtonAnimationListener.onExpandAnimationEnd(GplaySpriteButton.this);
                    }
                }
            });
            mLayoutExpandAnimation.start(mSuspendButtonView, initPositionX, initPositionY, endPositionX, endPositionY, mButtonSize, mExpandDuration);
        }
    }

    public void shrink() {
        if(null != mLayoutShrinkAnimation) {
            mLayoutShrinkAnimation.setAnimationListener(new LayoutAnimation.OnLayoutAnimationListener() {
                @Override
                public void onAnimationStart() {
                    if(null != mSuspensionButtonAnimationListener) {
                        mSuspensionButtonAnimationListener.onShrinkAnimationStart(GplaySpriteButton.this);
                    }
                }

                @Override
                public void onAnimation(int curX, int curY) {
                    if(null != mSuspensionButtonAnimationListener) {
                        mSuspensionButtonAnimationListener.onShrinkAnimation(GplaySpriteButton.this, curX, curY);
                    }
                }

                @Override
                public void onAnimationEnd() {
                    if(null != mSuspensionButtonAnimationListener) {
                        mSuspensionButtonAnimationListener.onShrinkAnimationEnd(GplaySpriteButton.this);
                    }
                }
            });
            mLayoutShrinkAnimation.start(mSuspendButtonView, mEndPositionX, mEndPositionY, mStartPositionX, mStartPositionY - mButtonSize, mButtonSize, mShrinkDuration);
        }
    }

    public void setGplayButtonAction(OnGplayButtonAction buttonAction) {
        mGplayButtonAction = buttonAction;
    }

    private class SuspendButtonView extends View {
        private int mLayoutWidth;

        public SuspendButtonView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int left = mLayoutWidth-mButtonSize;
            int top = 0;
            mIcon.setBounds(left, top, left + mButtonSize, top + mButtonSize);
            mIcon.draw(canvas);
            canvas.drawText(mTitle, 0, 0, new Paint());
            super.onDraw(canvas);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mLayoutWidth = right - left;
            this.invalidate();
        }
    }

    interface OnGplaySuspensionButtonAnimationListener {
        void onExpandAnimationStart(GplaySpriteButton button);
        void onExpandAnimation(GplaySpriteButton button, int curX, int curY);
        void onExpandAnimationEnd(GplaySpriteButton button);
        void onShrinkAnimationStart(GplaySpriteButton button);
        void onShrinkAnimation(GplaySpriteButton button, int curX, int curY);
        void onShrinkAnimationEnd(GplaySpriteButton button);
    }

    interface OnSuspensionButtonClickListener {
        void onSuspensionButtonClick(GplaySpriteButton btn);
    }

    @Override
    public void onClick(View v) {
        if(null != mGplayButtonAction) {
            mGplayButtonAction.onInvoke();
            mSuspensionButtonClickListener.onSuspensionButtonClick(GplaySpriteButton.this);
        }
    }
}
