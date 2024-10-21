package com.ideacarry.example28

import android.content.Context
import android.opengl.GLSurfaceView
import androidx.lifecycle.DefaultLifecycleObserver
import com.ideacarry.utils.GLShaderProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Raining on 2023/08/15
 *
 * 实现视频画面渲染(surface)
 */
class VideoGLRenderer2(
    private val context: Context,
) : GLSurfaceView.Renderer, DefaultLifecycleObserver {

    private var shaderProgram: GLShaderProgram? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        TODO("Not yet implemented")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun onDrawFrame(gl: GL10?) {
        TODO("Not yet implemented")
    }
}