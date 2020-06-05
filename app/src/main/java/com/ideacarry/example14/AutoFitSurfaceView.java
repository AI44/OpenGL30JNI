package com.ideacarry.example14;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

public class AutoFitSurfaceView extends SurfaceView {

    private View mParent;
    private int mWidth;
    private int mHeight;

    public AutoFitSurfaceView(Context context) {
        super(context);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSize(View parent, int w, int h) {
        mParent = parent;
        mWidth = w;
        mHeight = h;
        getHolder().setFixedSize(mWidth, mHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mParent != null) {
            int w;
            int h;
            if (PreviewUtils.isNaturalPortrait(this)) {
                w = mHeight;
                h = mWidth;
            } else {
                w = mWidth;
                h = mHeight;
            }
            //缩放
            float scale = Math.min((float) mParent.getWidth() / w, (float) mParent.getHeight() / h);
            w = (int) (w * scale + 0.5f);
            h = (int) (h * scale + 0.5f);
            setMeasuredDimension(w, h);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
