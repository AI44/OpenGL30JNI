package com.ideacarry.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Environment;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Created by Raining on 2020/6/22.
 */
public class CommonUtils {
    /*
     ****************************************************************
     * 文件读取/文件夹创建
     ****************************************************************
     */
    public static byte[] ReadData(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(2048);

        byte[] buf = new byte[1024];
        byte[] out = null;
        int readSize = 0;
        try {
            while ((readSize = is.read(buf)) > -1) {
                os.write(buf, 0, readSize);
            }
            out = os.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return out;
    }

    /**
     * 读数据
     */
    public static byte[] ReadData(int size, InputStream is) {
        if (size < 1) {
            //读取系统未知大小文件
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            final int LEN = 1024;
            final byte[] BUF = new byte[LEN];
            byte[] ret = null;
            int readSize = 0;
            try {
                while ((readSize = is.read(BUF, 0, LEN)) > -1) {
                    out.write(BUF, 0, readSize);
                }
                ret = out.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return ret;
        } else {
            //已知文件大小方式，效率高
            byte[] out = new byte[size];
            int currentSize = 0;
            int readSize = 0;
            try {
                while ((readSize = is.read(out, currentSize, size - currentSize)) > -1) {
                    currentSize += readSize;
                    if (currentSize >= size) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return out;
        }
    }

    /**
     * 加载本地文件
     */
    public static byte[] ReadFile(String path) {
        byte[] out = null;

        File file = new File(path);
        if (file.exists()) {
            int totalSize = (int) file.length();
            if (totalSize >= 0) {
                FileInputStream is = null;
                try {
                    is = new FileInputStream(file);
                    out = ReadData(totalSize, is);
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return out;
    }

    public static void MakeFolder(String path) {
        try {
            if (path != null) {
                File file = new File(path);
                if (!(file.exists() && file.isDirectory())) {
                    file.mkdirs();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static boolean MakeParentFolder(String path) {
        boolean out = false;

        if (path != null) {
            File file = new File(path).getParentFile();
            if (file != null) {
                if (file.exists()) {
                    out = true;
                } else {
                    if (file.mkdirs()) {
                        out = true;
                    }
                }
            }
        }

        return out;
    }

    public static boolean SaveFile(String path, byte[] data) {
        boolean out = false;

        FileOutputStream fos = null;
        try {
            if (path != null && data != null) {
                if (MakeParentFolder(path)) {
                    fos = new FileOutputStream(path);
                    fos.write(data);
                    fos.close();
                    fos = null;
                    out = true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable e) {
                }
                fos = null;
            }
        }

        return out;
    }

    public static boolean SaveFile(String path, InputStream is) {
        boolean out = false;

        FileOutputStream fos = null;
        try {
            if (path != null && is != null) {
                if (MakeParentFolder(path)) {
                    fos = new FileOutputStream(path);
                    final int BUF_SIZE = 8192;
                    int len;
                    byte[] buffer = new byte[BUF_SIZE];
                    while ((len = is.read(buffer, 0, BUF_SIZE)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    fos = null;
                    out = true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable e) {
                }
            }
        }

        return out;
    }

    public interface ProgressListener {
        void onProgress(int progress);
    }

    public static boolean SaveFile(String path, InputStream is, long size, ProgressListener lst) {
        if (lst == null) {
            return SaveFile(path, is);
        }

        boolean out = false;
        FileOutputStream fos = null;
        try {
            if (path != null && is != null) {
                if (MakeParentFolder(path)) {
                    fos = new FileOutputStream(path);
                    final int BUF_SIZE = 8192;
                    int len;
                    int total = 0;
                    long cur = System.currentTimeMillis();
                    long temp;
                    byte[] buffer = new byte[BUF_SIZE];
                    lst.onProgress(1);
                    while ((len = is.read(buffer, 0, BUF_SIZE)) != -1) {
                        total += len;
                        fos.write(buffer, 0, len);
                        if (size > 0) {
                            temp = System.currentTimeMillis();
                            if (temp - cur > 1000) {
                                cur = temp;
                                int p = (int) ((float) total / size * 100);
                                if (p > 100) {
                                    p = 99;
                                }
                                lst.onProgress(p);
                            }
                        }
                    }
                    lst.onProgress(100);
                    fos.close();
                    fos = null;
                    out = true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable e) {
                }
            }
        }

        return out;
    }

    public static String getPhotoSavePath() {
        String out = null;
        String manufacturer = android.os.Build.MANUFACTURER;
        if (manufacturer != null) {
            manufacturer = manufacturer.toLowerCase(Locale.getDefault());
            //魅族的默认相册路径不同，原来的路径图库不显示
            if (manufacturer.contains("meizu")) {
                out = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Camera";
            }
        }
        if (out == null) {
            out = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "相机";
            if (!new File(out).exists()) {
                out = null;
            }
        }
        if (out == null) {
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            if (dcim != null) {
                out = dcim.getAbsolutePath() + File.separator + "Camera";
            } else {
                out = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM" + File.separator + "Camera";
            }
        }
        return out;
    }

    /**
     * 获取真实版本号
     */
    public static String GetAppVer(Context context) {
        String out = null;

        try {
            PackageManager pm = context.getApplicationContext().getPackageManager();
            if (pm != null) {
                PackageInfo pi = pm.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
                if (pi != null) {
                    out = pi.versionName;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return out;
    }

    /**
     * 获取真实版本号
     */
    public static int GetAppVerCode(Context context) {
        int out = 0;

        try {
            PackageManager pm = context.getApplicationContext().getPackageManager();
            if (pm != null) {
                PackageInfo pi = pm.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
                if (pi != null) {
                    out = pi.versionCode;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return out;
    }

    public static void PointFArrayToFloatArray(float[] dst, int dstOffset, PointF[] arr) {
        if (arr != null && dst.length - dstOffset >= (arr.length >> 1)) {
            int i = dstOffset;
            for (PointF point : arr) {
                dst[i] = point.x;
                dst[i + 1] = point.y;
                i += 2;
            }
        }
    }

    public static float[] PointFArrayToFloatArray(PointF[] arr) {
        if (arr != null) {
            float[] result = new float[arr.length << 1];
            PointFArrayToFloatArray(result, 0, arr);
            return result;
        }
        return null;
    }

    /**
     * 放在Activity的onStart里实现全屏<br/>
     * <pre>
     * {@code
     * protected void onStart() {
     *     CommonUtils.activityFullScreen(this);
     *     super.onStart();
     * }
     * }
     * </pre>
     */
    public static void activityFullScreen(final Activity activity) {
        if (activity != null) {
            final View decorView = activity.getWindow().getDecorView();
            int flag = decorView.getSystemUiVisibility();
            flag |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;//固定开始的布局，不随navigation bar显示/隐藏影响
            flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;//设置后布局会被status bar和navigation bar覆盖
            flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;//设置后布局会被status bar覆盖
            flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;//隐藏navigation bar
            flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;//bar 唤出后几秒就消失，不触发 Listener
            decorView.setSystemUiVisibility(flag);
        }
    }

    public static byte[] readAssetFile(Context context, String path) {
        try {
            return ReadData(context.getAssets().open(path));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
