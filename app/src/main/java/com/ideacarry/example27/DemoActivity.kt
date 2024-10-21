package com.ideacarry.example27

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.ideacarry.example27.decoder.DecoderHelper
import com.ideacarry.example27.decoder.FrameData
import com.ideacarry.example27.decoder.OnUpdateFrameListener
import com.ideacarry.example27.player.SyncHandler
import com.ideacarry.example27.player.VideoGLRenderer
import com.ideacarry.example27.player.VideoTimestampProvider
import com.ideacarry.opengl30jni.databinding.Example27Binding
import kotlin.math.roundToInt

/**
 * Created by Raining on 2023/1/5
 *
 * #I# MediaCodec异步解码音视频(YUV)
 */
class DemoActivity : AppCompatActivity() {

    private val viewBinding by lazy {
        Example27Binding.inflate(LayoutInflater.from(this))
    }
    private val renderer by lazy {
        VideoGLRenderer(applicationContext)
    }
    private val syncHandler by lazy {
        SyncHandler(this, VideoTimestampProvider(), object : OnUpdateFrameListener {
            override fun onUpdate(data: FrameData) {
                viewBinding.player.queueEvent {
                    renderer.updateDraw(data.buffer, data.info, data.format)
                    viewBinding.player.requestRender()
                    data.renderFinish(true)
                }
                viewBinding.root.post {
                    val percent = data.info.presentationTimeUs.toFloat() / data.durationUs.coerceAtLeast(1)
                    viewBinding.progressBar.progress = (percent * MAX_PROGRESS).roundToInt()
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initUI()
        configData()
    }

    private fun initUI() {
        viewBinding.player.setEGLContextClientVersion(3)
        viewBinding.player.setRenderer(renderer)
        viewBinding.player.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        viewBinding.testBtn.setOnClickListener {
            viewBinding.player.requestRender()
        }
        lifecycle.addObserver(renderer)
    }

    private fun configData() {
        val path = "/sdcard/DCIM/Camera/PXL_20230609_112959044.mp4"
        val path2 = "/sdcard/DCIM/Camera/PXL_20230609_113049874.mp4"
        val path3 = "/sdcard/video/HEVC_320_AAC_128.mp4"
        val path4 = "/sdcard/video/test.mp4"
        val path5 = "/sdcard/video/test_video.mp4"
        val path6 = "/sdcard/video/vid_bigbuckbunny.mp4"
        val path7 = DecoderHelper.getPathForAssetsFile("example25/test.mp4")
        //Log.d(TAG, "${DecoderHelper.getVideoInfo(path)}")
        //Log.d(TAG, "${DecoderHelper.getVideoInfo(path2)}")
        //Log.d(TAG, "${DecoderHelper.getVideoInfo(path3)}")
        //Log.d(TAG, "${DecoderHelper.getVideoInfo(path4)}")
        //Log.d(TAG, "${DecoderHelper.getEncoderYuvStandard().toTypedArray().contentToString()}")
        //Log.d(TAG, "${DecoderHelper.getYuvStandardFromVideo(path)}")
        //Log.d(TAG, "${DecoderHelper.getYuvStandardFromVideo(path2)}")
        //Log.d(TAG, "${DecoderHelper.getYuvStandardFromVideo(path3)}")
        //Log.d(TAG, "${DecoderHelper.getYuvStandardFromVideo(path4)}")

        syncHandler.setData(path7)
    }

    override fun onDestroy() {
        syncHandler.release()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "example27"
        private const val MAX_PROGRESS = 1000
    }
}

