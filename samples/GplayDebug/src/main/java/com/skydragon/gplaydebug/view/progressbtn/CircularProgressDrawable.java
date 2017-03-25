package com.skydragon.gplaydebug.view.progressbtn;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;

class CircularProgressDrawable extends Drawable {

    private float mSweepAngle;
    private float mStartAngle;
    private int mSize;
    private int mStrokeWidth;
    private int mStrokeColor;
    private int mProgress;

    public CircularProgressDrawable(int size, int strokeWidth, int strokeColor) {
        mSize = size;
        mStrokeWidth = strokeWidth;
        mStrokeColor = strokeColor;
        mStartAngle = -90;
        mSweepAngle = 0;
    }

    public void setSweepAngle(float sweepAngle) {
        mSweepAngle = sweepAngle;
    }

    public int getSize() {
        return mSize;
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect bounds = getBounds();

        if (mPath == null) {
            mPath = new Path();
        }
        mPath.reset();
        mPath.addArc(getRect(), mStartAngle, mSweepAngle);
        mPath.offset(bounds.left, bounds.top);
        canvas.drawPath(mPath, createPaint());

        Log.v("", " bounds : " + bounds);

        mTextPaint = getTextPaint();
        String percentStr = mProgress + "%";
        Rect txtBounds = new Rect();
        mTextPaint.getTextBounds(percentStr, 0, percentStr.length(), txtBounds);

        Log.v("", " txtBounds : " + txtBounds);
        Log.v("", " x : " + bounds.exactCenterX() + "  , y  " + bounds.exactCenterY());
        Log.v("", " left  : " + bounds.left + "  , top  " + bounds.top);

        canvas.drawText(percentStr, mSize / 2 + bounds.centerX() - txtBounds.centerX(), mSize / 2 + bounds.centerY() - txtBounds.centerY(), mTextPaint);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return 1;
    }

    private RectF mRectF;
    private Paint mPaint;
    private Paint mTextPaint;
    private Path mPath;

    private RectF getRect() {
        if (mRectF == null) {
            int index = mStrokeWidth / 2;
            mRectF = new RectF(index, index, getSize() - index, getSize() - index);
        }
        return mRectF;
    }

    private Paint createPaint() {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWidth);
            mPaint.setColor(mStrokeColor);
        }
        return mPaint;
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }

    public Paint getTextPaint() {
        if (mTextPaint == null) {
            mTextPaint = new Paint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setStyle(Paint.Style.FILL);
            mTextPaint.setTextSize(40);
            mTextPaint.setStrokeWidth(3);
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            mTextPaint.setColor(mStrokeColor);
        }
        return mTextPaint;
    }

    public Paint getTestPaints() {
        Paint testPaints = new Paint();
        testPaints.setStrokeWidth(mStrokeWidth);
        testPaints.setColor(mStrokeColor);
        return testPaints;
    }
}
