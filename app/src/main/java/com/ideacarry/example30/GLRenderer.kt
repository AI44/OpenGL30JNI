package com.ideacarry.example30

import android.content.Context
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer(
    private val context: Context
) : GLSurfaceView.Renderer {

    override fun onDrawFrame(gl: GL10?) {
        drawFrame()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceChanged(width, height)
    }

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        surfaceCreated(context.cacheDir.absolutePath)
    }

    external fun drawFrame()

    external fun surfaceCreated(resDir: String)

    external fun surfaceChanged(width: Int, height: Int)

    external fun release()
}