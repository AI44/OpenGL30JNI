package com.ideacarry.example14;

import android.graphics.Point;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.View;

import androidx.annotation.NonNull;

public class PreviewUtils {
    public static boolean isNaturalPortrait(@NonNull final View view) {
        final Display display = view.getDisplay();
        if (display == null) {
            return true;
        }

        final Point deviceSize = new Point();
        display.getRealSize(deviceSize);

        final int width = deviceSize.x;
        final int height = deviceSize.y;
        final int rotationDegrees = (int) getRotationDegrees(view);
        return ((rotationDegrees == 0 || rotationDegrees == 180) && width < height) || (
                (rotationDegrees == 90 || rotationDegrees == 270) && width >= height);
    }

    public static float getRotationDegrees(@NonNull final View view) {
        final Display display = view.getDisplay();
        if (display == null) {
            return 0;
        }
        final int rotation = display.getRotation();
        return rotationDegreesFromSurfaceRotation(rotation);
    }

    public static int rotationDegreesFromSurfaceRotation(final int rotationConstant) {
        switch (rotationConstant) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                throw new UnsupportedOperationException(
                        "Unsupported surface rotation constant: " + rotationConstant);
        }
    }

    public static Size getPreviewSize(@NonNull final View view, @NonNull Size cameraSize) {
        if (isNaturalPortrait(view)) {
            return new Size(cameraSize.getHeight(), cameraSize.getWidth());
        } else {
            return new Size(cameraSize.getWidth(), cameraSize.getHeight());
        }
    }
}
