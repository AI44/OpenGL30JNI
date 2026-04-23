package com.ideacarry.utils

import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.os.Build
import android.os.Environment
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale

/**
 * Created by Raining on 2020/6/22.
 */
object CommonUtils {
    /*
     ****************************************************************
     * 文件读取/文件夹创建
     ****************************************************************
     */
    fun readData(inputStream: InputStream): ByteArray? {
        return try {
            ByteArrayOutputStream(2048).use { os ->
                val buf = ByteArray(1024)
                var readSize: Int
                while (inputStream.read(buf).also { readSize = it } > -1) {
                    os.write(buf, 0, readSize)
                }
                os.toByteArray()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 读数据
     */
    fun readData(size: Int, inputStream: InputStream): ByteArray? {
        return if (size < 1) readDataUnknownSize(inputStream)
        else readDataKnownSize(size, inputStream)
    }

    private fun readDataUnknownSize(inputStream: InputStream): ByteArray? {
        return try {
            ByteArrayOutputStream(4096).use { out ->
                val buf = ByteArray(1024)
                var readSize: Int
                while (inputStream.read(buf, 0, buf.size).also { readSize = it } > -1) {
                    out.write(buf, 0, readSize)
                }
                out.toByteArray()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun readDataKnownSize(size: Int, inputStream: InputStream): ByteArray {
        val out = ByteArray(size)
        var currentSize = 0
        try {
            while (currentSize < size) {
                val readSize = inputStream.read(out, currentSize, size - currentSize)
                if (readSize < 0) break
                currentSize += readSize
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return out
    }

    /**
     * 加载本地文件
     */
    fun readFile(path: String): ByteArray? {
        val file = File(path)
        if (!file.exists()) return null
        val totalSize = file.length().toInt()
        if (totalSize < 0) return null
        return try {
            FileInputStream(file).use { readData(totalSize, it) }
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    fun makeFolder(path: String?) {
        path ?: return
        try {
            val file = File(path)
            if (!file.exists() || !file.isDirectory) file.mkdirs()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun makeParentFolder(path: String?): Boolean {
        val parent = File(path ?: return false).parentFile ?: return false
        return parent.exists() || parent.mkdirs()
    }

    fun saveFile(path: String?, data: ByteArray?): Boolean {
        if (path == null || data == null || !makeParentFolder(path)) return false
        return try {
            FileOutputStream(path).use { it.write(data) }
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    fun saveFile(path: String?, inputStream: InputStream?): Boolean {
        if (path == null || inputStream == null || !makeParentFolder(path)) return false
        return try {
            FileOutputStream(path).use { fos ->
                val buffer = ByteArray(8192)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    fos.write(buffer, 0, len)
                }
            }
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    fun saveFile(path: String?, inputStream: InputStream?, size: Long, lst: ProgressListener?): Boolean {
        if (lst == null) return saveFile(path, inputStream)
        if (path == null || inputStream == null || !makeParentFolder(path)) return false
        return try {
            FileOutputStream(path).use { fos ->
                val buffer = ByteArray(8192)
                var len: Int
                var total = 0
                var lastReport = System.currentTimeMillis()
                lst.onProgress(1)
                while (inputStream.read(buffer).also { len = it } != -1) {
                    total += len
                    fos.write(buffer, 0, len)
                    lastReport = maybeReportProgress(lst, size, total, lastReport)
                }
                lst.onProgress(100)
            }
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    private fun maybeReportProgress(lst: ProgressListener, size: Long, total: Int, lastReport: Long): Long {
        if (size <= 0) return lastReport
        val now = System.currentTimeMillis()
        if (now - lastReport <= 1000) return lastReport
        lst.onProgress(minOf((total.toFloat() / size * 100).toInt(), 99))
        return now
    }

    val photoSavePath: String
        get() {
            val manufacturer = Build.MANUFACTURER?.lowercase(Locale.getDefault())
            if (manufacturer?.contains("meizu") == true) {
                return Environment.getExternalStorageDirectory().absolutePath + File.separator + "Camera"
            }
            val chinesePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + "相机"
            if (File(chinesePath).exists()) return chinesePath
            val dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val base = dcim?.absolutePath
                ?: (Environment.getExternalStorageDirectory().absolutePath + File.separator + "DCIM")
            return base + File.separator + "Camera"
        }

    /**
     * 获取真实版本号
     */
    fun getAppVer(context: Context): String? {
        return try {
            context.applicationContext.packageManager
                ?.getPackageInfo(context.applicationContext.packageName, 0)
                ?.versionName
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取真实版本号
     */
    fun getAppVerCode(context: Context): Int {
        return try {
            context.applicationContext.packageManager
                ?.getPackageInfo(context.applicationContext.packageName, 0)
                ?.versionCode ?: 0
        } catch (e: Throwable) {
            e.printStackTrace()
            0
        }
    }

    fun pointFArrayToFloatArray(dst: FloatArray, dstOffset: Int, arr: Array<PointF>?) {
        arr ?: return
        if (dst.size - dstOffset < arr.size shr 1) return
        var i = dstOffset
        for (point in arr) {
            dst[i] = point.x
            dst[i + 1] = point.y
            i += 2
        }
    }

    fun pointFArrayToFloatArray(arr: Array<PointF>?): FloatArray? {
        arr ?: return null
        val result = FloatArray(arr.size shl 1)
        pointFArrayToFloatArray(result, 0, arr)
        return result
    }

    /**
     * 放在Activity的onStart里实现全屏<br></br>
     * <pre>
     * `protected void onStart() {     CommonUtils.activityFullScreen(this);     super.onStart(); } `
    </pre> *
     */
    @JvmStatic
    fun activityFullScreen(activity: Activity?) {
        activity ?: return
        val decorView = activity.window.decorView
        decorView.systemUiVisibility = decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or         // 固定开始的布局，不随navigation bar显示/隐藏影响
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or // 布局会被status bar和navigation bar覆盖
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or      // 布局会被status bar覆盖
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or        // 隐藏navigation bar
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY          // bar 唤出后几秒就消失，不触发 Listener
    }

    @JvmStatic
    fun readAssetFile(context: Context, path: String): ByteArray? {
        return try {
            context.assets.open(path).use { readData(it) }
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun copyAssetsDirToInternalStorage(context: Context, assetsDirName: String, sdCardDir: String) {
        var dstDir = sdCardDir
        try {
            val list = context.assets.list(assetsDirName) ?: return
            if (list.isEmpty()) {
                copyAssetFileToStorage(context, assetsDirName, dstDir)
                return
            }
            val subDirName = if (assetsDirName.contains("/"))
                assetsDirName.substring(assetsDirName.lastIndexOf('/') + 1)
            else assetsDirName
            dstDir = "$dstDir${File.separator}$subDirName"
            File(dstDir).mkdirs()
            for (s in list) {
                copyAssetsDirToInternalStorage(context, "$assetsDirName${File.separator}$s", dstDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyAssetFileToStorage(context: Context, assetPath: String, destDir: String) {
        val file = File("$destDir${File.separator}${assetPath.substring(assetPath.lastIndexOf('/'))}")
        if (file.exists()) return
        file.createNewFile()
        context.assets.open(assetPath).use { inputStream ->
            FileOutputStream(file).use { fos ->
                val buf = ByteArray(1024)
                var bt: Int
                while (inputStream.read(buf).also { bt = it } != -1) {
                    fos.write(buf, 0, bt)
                }
                fos.flush()
            }
        }
    }

    interface ProgressListener {
        fun onProgress(progress: Int)
    }
}
