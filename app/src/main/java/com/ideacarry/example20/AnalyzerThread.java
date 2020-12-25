package com.ideacarry.example20;

import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

public class AnalyzerThread extends HandlerThread implements Executor, ImageAnalysis.Analyzer {

    private Handler mHandler;

    public AnalyzerThread() {
        super("AnalyzerThread");
    }

    private byte[] mBuffer;

    private byte[] getTempBuffer(int size) {
        if (mBuffer == null || mBuffer.length != size) {
            mBuffer = new byte[size];
        }
        return mBuffer;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            int w = image.getWidth();
            int h = image.getHeight();
            byte[] buf = getTempBuffer(w * h + ((((w + 1) >> 1) * ((h + 1) >> 1)) << 1));
            ImageProxy.PlaneProxy[] proxies = image.getPlanes();
            if (proxies.length > 1) {
                int offset = 0;
                for (int i = 0; i < 3; i++) {
                    switch (i) {
                        case 0:
                        case 2:
                            ByteBuffer byteBuffer = proxies[i].getBuffer();
                            int size = byteBuffer.remaining();
                            byteBuffer.get(buf, offset, size);
                            offset += size;
                            break;
                    }
                }
                //这里做人脸检测-----------------------------------------------------------------start
                //buf
                //这里做人脸检测-------------------------------------------------------------------end
            }
        }
        image.close();
    }

    public void setDegree(final int phoneDegree, final int cameraDegree) {
        //获取到镜头和重力感应的角度，用于设置到人脸识别库
    }

    @Override
    public void execute(Runnable command) {
        getHandler().post(command);
    }

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(getLooper());
        }
        return mHandler;
    }
}
