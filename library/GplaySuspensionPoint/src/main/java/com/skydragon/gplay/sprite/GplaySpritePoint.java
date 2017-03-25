package com.skydragon.gplay.sprite;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;


public class GplaySpritePoint implements View.OnTouchListener, GplaySpriteButton.OnGplaySuspensionButtonAnimationListener, GplaySpriteButton.OnSuspensionButtonClickListener{
    private static final String TAG = "GplayWhitePoint";

    private int mWhitePointSize;

    private Activity mAct;
    private ViewGroup mParentContainer;
    private int mStartX;
    private int mStartY;
    private Drawable mWhitePoint;
    private GplaySpriteBackground mContainer;
    private View mIvPoint;
    private int mSpacing;

    private Drawable mExpandDrawable;

    private int mScreenWidth;
    private int mScreenHeight;

    private int mCurrentLeft;
    private int mCurrentTop;

    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    private boolean mIsAnimation;

    private boolean mIsDragoning;

    private boolean mIsExpading;

    private OnGplaySpriteDragonListener mDragonListener;

    private LayoutAnimation mMovableAnimation;

    private LayoutAnimation mHideAnimation;

    private LayoutAnimation mShowAnimation;

    private List<GplaySpriteButton> mListSuspensionButton;

    private int mTouchSlopSquare;

    public GplaySpritePoint(Activity act, ViewGroup parentContainer, int startX, int startY, Drawable dWhitePoint, int whitePointSize, int spacing) {
        mAct = act;
        mParentContainer = parentContainer;
        mStartX = startX;
        mStartY = startY;
        mWhitePoint = dWhitePoint;
        mSpacing = spacing;

        mCurrentLeft = startX;
        mCurrentTop = startY;

        mWhitePointSize = whitePointSize;

        init(act);
    }

    public void addSuspendButton(GplaySpriteButton suspensionButton) {
        mListSuspensionButton.add(suspensionButton);
        suspensionButton.setGplaySuspensionButtonAnimationListener(this);
        suspensionButton.setGplaySuspensionButtonClickListener(this);
        suspensionButton.setParentSize(mWhitePointSize);
        suspensionButton.setSpacing(mSpacing);
    }

    /**
     * 设置拖拉监听器
     * @param listener
     */
    public void setDragonListener(OnGplaySpriteDragonListener listener) {
        mDragonListener = listener;
    }

    /**
     * 设置移动动画
     * @param animation
     */
    public void setMovableAnimation(LayoutAnimation animation) {
        mMovableAnimation = animation;
    }

    /**
     * 设置隐藏动画
     * @param animation
     */
    public void setHideAnimation(LayoutAnimation animation) {
        mHideAnimation = animation;
    }

    /**
     * 设置小白点展开背景色
     * @param d
     */
    public void setExpandBackground(Drawable d) {
        mExpandDrawable = d;
        if(null != mExpandDrawable) {
            mContainer.setExpandBackground(mExpandDrawable);
        }
    }

    public void onResume() {
        start();
    }

    public void onPause() {
        hide();
    }

    public void onNewIntent() {
        start();
    }

    private boolean mIsClickEvent = true;
    private int mOriginalX;
    private int mOriginalY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        int action = event.getAction();

        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (action) {
            // 鼠标按下 拖拉动作开始
            case MotionEvent.ACTION_DOWN:
                mIsDragoning = true;
                mUiHandler.removeCallbacks(mAutoMovableTask);
                mCurrentTop = v.getTop();

                mStartX = (int) event.getX();
                mStartY = y - v.getTop();

                mOriginalX = x;
                mOriginalY = y;
                if(null != mDragonListener) {
                    mDragonListener.onGplayWhitePointDragonStart(mStartX, mStartY);
                }
                break;

            // 鼠标移动 拖拉动作进行中
            case MotionEvent.ACTION_MOVE:
                if(mIsExpading) return false;
                int tempX = x - mStartX;
                int tempY = y - mStartY;
                mCurrentLeft = tempX;
                mCurrentTop = tempY;

                if( mIsClickEvent ) {
                    if(Math.abs(x - mOriginalX) > mTouchSlopSquare || Math.abs(y - mOriginalY) > mTouchSlopSquare) {
                        mIsClickEvent = false;
                    }
                }
                if(isPositionValid()) {
                    v.layout(tempX, tempY, tempX + v.getWidth(), tempY + v.getHeight());
                } else {
                    int[] adjustPoint = getValidPosition();
                    mCurrentLeft = adjustPoint[0];
                    mCurrentTop = adjustPoint[1];
                    v.layout(mCurrentLeft, mCurrentTop, mCurrentLeft + v.getWidth(), mCurrentTop + v.getHeight());
                }
                if(null != mDragonListener) {
                    mDragonListener.onGplayWhitePointDragonMove(mCurrentLeft, mCurrentTop);
                }
                break;
            // 鼠标释放 拖拉动作结束
            case MotionEvent.ACTION_UP:
                mIsDragoning = false;
                if(mIsAnimation) return false;
                if(null != mDragonListener) {
                    mDragonListener.onGplayWhitePointDragonUp(mCurrentLeft, mCurrentTop);
                }
                Log.d(TAG, "GplaySuspensionPoint IsClickEvent:" + mIsClickEvent);
                if(mIsClickEvent) {
                    Log.d(TAG, "GplaySuspensionPoint action up isExpading=" + mIsExpading);
                    if(mIsExpading) {
                        shrink();
                    } else {
                        expand();
                    }
                    return false;
                }
                mIsClickEvent = true;
                if(null != mMovableAnimation) {
                    mMovableAnimation.setAnimationListener(new LayoutAnimation.OnLayoutAnimationListener() {
                        @Override
                        public void onAnimationStart() {
                            mIsAnimation = true;
                        }

                        @Override
                        public void onAnimation(int curX, int curY) {
                            mCurrentLeft = curX;
                            mCurrentTop = curY;
                        }

                        @Override
                        public void onAnimationEnd() {
                            showGplayPointAtEdge();
                            mIsAnimation = false;
                        }
                    });
                }

                int toX = mCurrentLeft;
                int toY = mCurrentTop;
                if(mCurrentLeft > mScreenWidth/2) {
                    toX = mScreenWidth - mWhitePointSize;
                } else {
                    toX = 0;
                }
                if(null != mMovableAnimation) {
                    mMovableAnimation.start(mIvPoint, mCurrentLeft, mCurrentTop, toX, toY, mWhitePointSize, 500);
                } else {
                    showGplayPointAtEdge();
                }
                break;
        }
        return false;
    }

    private void init(Activity act) {
        Point pointScreen = getScreenSize();
        mScreenWidth = pointScreen.x;
        mScreenHeight = pointScreen.y;

        Log.d(TAG, "screenwidth=" + mScreenWidth + ",screenheight=" + mScreenHeight);
        mMovableAnimation = new LayoutDecelerateAnimation();
        mHideAnimation = new LayoutDecelerateAnimation();
        mShowAnimation = new LayoutDecelerateAnimation();

        ViewConfiguration configuration = ViewConfiguration.get(act);
        mTouchSlopSquare = configuration.getScaledTouchSlop();

        mListSuspensionButton = new ArrayList<GplaySpriteButton>();

        GplaySpriteBackground bg = new GplaySpriteBackground(mAct);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT );
        bg.setLayoutParams(params);
        mContainer = bg;
    }

    public void start() {

        mParentContainer.removeView(mContainer);
        mParentContainer.addView(mContainer, -1);

        ImageView iv = new ImageView(mAct);
        iv.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        iv.setImageDrawable(mWhitePoint);
        mIvPoint = iv;
        iv.setVisibility(View.INVISIBLE);
        mContainer.addView(iv, -1);

        iv.setOnTouchListener(this);
        iv.setLongClickable(true);

        mContainer.setOnSuspensionBackgroundClickListener(new GplaySpriteBackground.OnSuspensionBackgroundClickListener() {
            @Override
            public void onSuspensionBackGroundClick() {
                shrink();
            }
        });

        showGplayPointAtEdge();
    }

    private void hide() {
        mContainer.removeAllViews();
        mParentContainer.removeView(mContainer);
    }

    private void expand() {
        if(mIsDragoning) return;
        int currentX = mCurrentLeft;
        mIsAnimation = true;
        mContainer.onExpand();
        if(mCurrentLeft <= mTouchSlopSquare && mCurrentLeft >= -mTouchSlopSquare) {
            mCurrentLeft = 0;
        } else if(mCurrentLeft >= (mScreenWidth-mWhitePointSize-mTouchSlopSquare) && mCurrentLeft <= (mScreenWidth-mWhitePointSize+mTouchSlopSquare)) {
            mCurrentLeft = mScreenWidth - mWhitePointSize;
        }

        if(mCurrentLeft == 0 || mCurrentLeft == (mScreenWidth - mWhitePointSize)) {
            int direction = mCurrentTop > mScreenHeight/2 ? GplaySpriteConstants.DIRECTION_UP: GplaySpriteConstants.DIRECTION_DOWN;
            int position = mCurrentLeft <= 0 ? GplaySpriteConstants.POSITION_LEFT : GplaySpriteConstants.POSITION_RIGHT;
            for(GplaySpriteButton button : mListSuspensionButton) {
                View view = button.getView();
                view.setVisibility(View.INVISIBLE);
                mContainer.removeView(view);
                mContainer.addView(view, 0);
                button.expand(currentX, mCurrentTop, direction, position);
            }
            mIsExpading = true;
        } else {
            if(mCurrentLeft < 0 || (mCurrentLeft + mWhitePointSize) > mScreenWidth ) {
                mShowAnimation.setAnimationListener(new LayoutAnimation.OnLayoutAnimationListener() {
                    @Override
                    public void onAnimationStart() {
                        mIsAnimation = true;
                    }

                    @Override
                    public void onAnimation(int curX, int curY) {

                    }

                    @Override
                    public void onAnimationEnd() {
                        mContainer.onExpand();
                        int position = mCurrentLeft <= 0 ? GplaySpriteConstants.POSITION_LEFT : GplaySpriteConstants.POSITION_RIGHT;
                        mCurrentLeft = (position == GplaySpriteConstants.POSITION_RIGHT)?mScreenWidth-mWhitePointSize:0;
                        int currentX = mCurrentLeft;
                        if(mCurrentLeft > mScreenWidth / 2 ) {
                            currentX = mScreenWidth - mWhitePointSize;
                        }
                        int direction = mCurrentTop > mScreenHeight/2 ? GplaySpriteConstants.DIRECTION_UP: GplaySpriteConstants.DIRECTION_DOWN;
                        for(GplaySpriteButton button : mListSuspensionButton) {
                            View view = button.getView();
                            view.setVisibility(View.INVISIBLE);
                            mContainer.addView(view, 0);
                            button.expand(currentX, mCurrentTop, direction, position);
                        }
                        mIsExpading = true;
                        mIsAnimation = false;
                    }
                });
                mShowAnimation.start(mIvPoint, mCurrentLeft, mCurrentTop, mCurrentLeft<0?0:(mScreenWidth-mWhitePointSize), mCurrentTop, mWhitePointSize, 200);
            }
        }
    }

    private void shrink() {
        if(mIsDragoning) return;
        mIsAnimation = true;
        for(GplaySpriteButton button : mListSuspensionButton) {
            button.shrink();
        }
        mIsExpading = false;
        mContainer.onCollapse();
    }

    private void showGplayPointAtEdge() {
        int toX = mCurrentLeft;
        int toY = mCurrentTop;
        if(mCurrentLeft > mScreenWidth/2) {
            toX = mScreenWidth - mWhitePointSize;
        } else {
            toX = 0;
        }

        mIvPoint.layout(toX, toY, toX + mWhitePointSize, toY + mWhitePointSize);
        mUiHandler.removeCallbacks(mAutoMovableTask);

        mUiHandler.postDelayed(mAutoMovableTask, 500);
    }

    private boolean isPositionValid() {
        if((mCurrentLeft + mWhitePointSize) >= mScreenWidth) return false;
        if(mCurrentLeft <= 0 ) return false;
        if((mCurrentTop + mWhitePointSize) >= mScreenHeight) return false;
        if(mCurrentTop <= 0 ) return false;
        return true;
    }

    private int[] getValidPosition() {
        int adjustX = mCurrentLeft, adjustY = mCurrentTop;
        if((mCurrentLeft + mWhitePointSize) >= mScreenWidth) {
            adjustX = mScreenWidth - mWhitePointSize;
        }
        if(mCurrentLeft <= 0 ) {
            adjustX = 0;
        }
        if((mCurrentTop + mWhitePointSize) >= mScreenHeight) {
            adjustY = mScreenHeight - mWhitePointSize;
        }
        if(mCurrentTop <= 0 ) {
            adjustY = 0;
        }
        return new int[]{adjustX,adjustY};
    }

    Runnable mAutoMovableTask = new Runnable() {
        @Override
        public void run() {
            if(mIsDragoning) return;
            int tempX = mCurrentLeft;
            if(mCurrentLeft <= 0) {
                mCurrentLeft = -mWhitePointSize/5 * 3;
            } else {
                mCurrentLeft = mScreenWidth - mWhitePointSize/5 * 2;
            }
            Log.d(TAG, "GplaySuspensionPoint autoMovableTask curX=" + tempX + ",curY=" + mCurrentTop + ",toX=" + mCurrentLeft + ",toY=" + mCurrentTop);
            if(null != mHideAnimation ) {
                mHideAnimation.start(mIvPoint, tempX, mCurrentTop, mCurrentLeft, mCurrentTop, mWhitePointSize, 200);
            } else {
                mIvPoint.layout(mCurrentLeft, mCurrentTop, mCurrentLeft + mWhitePointSize, mCurrentTop + mWhitePointSize);
            }
            if(null != mDragonListener) {
                mDragonListener.onGplayWhitePointDragonEnd(mCurrentLeft, mCurrentTop);
            }
        }
    };

    @Override
    public void onExpandAnimation(GplaySpriteButton button, int curX, int curY) {

    }

    @Override
    public void onExpandAnimationStart(GplaySpriteButton button) {

    }

    @Override
    public void onExpandAnimationEnd(GplaySpriteButton button) {
        mIsAnimation = false;
    }

    @Override
    public void onShrinkAnimationStart(GplaySpriteButton button) {

    }

    @Override
    public void onShrinkAnimation(GplaySpriteButton button, int curX, int curY) {

    }

    @Override
    public void onShrinkAnimationEnd(GplaySpriteButton button) {
        mContainer.removeView(button.getView());
        mUiHandler.removeCallbacks(mAutoMovableTask);
        mUiHandler.postDelayed(mAutoMovableTask, 500);
        mIsAnimation = false;
    }


    interface OnExpandAndCollapseListener {
        void onExpand();
        void onCollapse();
    }

    @Override
    public void onSuspensionButtonClick(GplaySpriteButton btn) {
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                shrink();
            }
        }, 100);
    }

    private Point getScreenSize() {
        WindowManager w = mAct.getWindowManager();
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        // since SDK_INT = 1;
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                widthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
                heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
            } catch (Exception ignored) {
            }
        }
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
                widthPixels = realSize.x;
                heightPixels = realSize.y;
            } catch (Exception ignored) {
            }
        }
        return new Point(widthPixels, heightPixels);
    }
}
