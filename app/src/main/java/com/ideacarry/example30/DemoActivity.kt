package com.ideacarry.example30

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.ideacarry.utils.CommonUtils

/**
 * @author Raining
 * @date 2026-04-14 15:08
 *
 * #I# assimp
 *
 */
class DemoActivity : AppCompatActivity() {
    private val glRenderer by lazy {
        GLRenderer(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fr = FrameLayout(this)
        setContentView(fr)

        CommonUtils.copyAssetsDirToInternalStorage(
            this,
            "example29",
            this.cacheDir.absolutePath
        )

        val glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(3)
        glSurfaceView.setRenderer(glRenderer)
        val fl = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        fr.addView(glSurfaceView, fl)
    }

    override fun onDestroy() {
        glRenderer.release()
        super.onDestroy()
    }
}