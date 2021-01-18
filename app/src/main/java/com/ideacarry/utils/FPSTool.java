package com.ideacarry.utils;

public class FPSTool {
    private final Callback mCallback;
    private long mStart;
    private int mCount;

    public FPSTool(Callback callback) {
        mCallback = callback;
    }

    public void count() {
        mCount++;
        long now = System.currentTimeMillis();
        if (mStart == 0) {
            mStart = now;
        }
        if (now - mStart > 999) {
            mCallback.update(mCount);
            mCount = 0;
            mStart = now;
        }
    }

    public void show() {
        long now = System.currentTimeMillis();
        long d = now - mStart;
        if (d < 1) {
            d = 1;
        }
        mCallback.update((int) (mCount / (double) d * 1000));
        mStart = now;
        mCount = 0;
    }

    public interface Callback {
        void update(int fps);
    }
}
