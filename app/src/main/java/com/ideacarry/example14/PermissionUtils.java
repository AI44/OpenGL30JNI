package com.ideacarry.example14;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    /**
     * 检查某个权限是否授权
     */
    public static boolean checkPermissions(@NonNull Context context, @NonNull String... permissions) {
        boolean out = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                out = false;
                break;
            }
        }
        return out;
    }
}
