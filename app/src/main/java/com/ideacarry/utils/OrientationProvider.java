package com.ideacarry.utils;

import android.content.Context;
import android.view.OrientationEventListener;
import android.view.Surface;

public class OrientationProvider extends OrientationEventListener {

    protected int mOrientation;
    protected DegreeListener mDegreeListener;

    public OrientationProvider(Context context) {
        super(context);
    }

    public OrientationProvider(Context context, int rate) {
        super(context, rate);
    }

    public void setDegreeListener(DegreeListener listener) {
        mDegreeListener = listener;
    }

    @Override
    public void onOrientationChanged(int orientation) {
        int now = mOrientation;
        if (60 < orientation && orientation < 120) {
            now = Surface.ROTATION_270;
        } else if (150 < orientation && orientation < 210) {
            now = Surface.ROTATION_180;
        } else if (240 < orientation && orientation < 300) {
            now = Surface.ROTATION_90;
        } else if (0 < orientation && orientation < 30 || 330 < orientation && orientation < 360) {
            now = Surface.ROTATION_0;
        }
        if (mDegreeListener != null) {
            boolean dif = now != mOrientation;
            mOrientation = now;
            if (dif) {
                mDegreeListener.onDegreeChange(getDegree());
            }
        } else {
            mOrientation = now;
        }

        //System.out.println("orientation : " + mOrientation);
    }

    /**
     * 返回方向
     *
     * @return {@link Surface#ROTATION_0},
     * {@link Surface#ROTATION_90},
     * {@link Surface#ROTATION_180},
     * {@link Surface#ROTATION_270}
     */
    public int getOrientation() {
        return mOrientation;
    }

    /**
     * 返回角度
     *
     * @return 0-360
     */
    public int getDegree() {
        switch (mOrientation) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    public interface DegreeListener {
        void onDegreeChange(int degree);
    }
}
