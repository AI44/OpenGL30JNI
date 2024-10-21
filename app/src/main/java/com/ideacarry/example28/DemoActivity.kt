package com.ideacarry.example28

import android.opengl.EGL14
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.android.grafika.gles.EglCore
import com.android.grafika.gles.OffscreenSurface
import com.ideacarry.example14.GLRenderer
import com.ideacarry.example18.DemoActivity
import com.ideacarry.opengl30jni.databinding.Example27Binding
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

/**
 * Created by Raining on 2023/08/15
 *
 * #I# MediaCodec异步解码视频(surface)
 */
class DemoActivity : AppCompatActivity() {

    private val viewBinding by lazy {
        Example27Binding.inflate(LayoutInflater.from(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initUI()
    }

    private fun initUI() {
        viewBinding.player.setEGLContextFactory(object : GLSurfaceView.EGLContextFactory {

            override fun createContext(egl: EGL10, display: EGLDisplay?, eglConfig: EGLConfig?): EGLContext {
                val attribList = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL10.EGL_NONE)
                return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attribList)
            }

            override fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?) {
                egl?.eglDestroyContext(display, context)
                //创建gl环境
                val eglCore = EglCore(EGL14.EGL_NO_CONTEXT, EglCore.FLAG_TRY_GLES3)
                val offscreenSurface = OffscreenSurface(eglCore, DemoActivity.W, DemoActivity.H)
                offscreenSurface.makeCurrent()
            }
        })
//        viewBinding.player.setEGLContextClientVersion(3)
//        viewBinding.player.setRenderer(renderer)
//        viewBinding.player.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
//        lifecycle.addObserver(renderer)
    }

    private fun configData() {

    }
}