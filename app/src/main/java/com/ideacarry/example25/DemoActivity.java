package com.ideacarry.example25;

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ideacarry.utils.FPSTool;

import java.nio.ByteBuffer;

public class DemoActivity extends AppCompatActivity {
    private static final String TAG = "example25";
    private static final String MP4_PATH = "example25/test.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            final AssetFileDescriptor afd = this.getAssets().openFd(MP4_PATH);
            synchronousDecodeVideo(afd);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void synchronousDecodeVideo(final AssetFileDescriptor afd) {
        new Thread(() -> {
            try {
                MediaExtractor extractor = new MediaExtractor();//解封装
                extractor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());

                int count = extractor.getTrackCount();
                int videoIndex = -1;
                MediaFormat videoFormat = null;
                //查找视频轨道
                for (int i = 0; i < count; i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                        videoIndex = i;
                        videoFormat = format;
                        break;
                    }
                }

                //解码
                if (videoIndex > -1) {
                    //获取视频信息
                    int width = videoFormat.getInteger(MediaFormat.KEY_WIDTH);
                    int height = videoFormat.getInteger(MediaFormat.KEY_HEIGHT);

                    extractor.selectTrack(videoIndex);//选择轨道

                    boolean inputDone = false;//数据是否填充完毕
                    boolean outputDone = false;
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    FPSTool fps = new FPSTool(value -> Log.i(TAG, "fps: " + value));

                    //选择解码器
                    String videoDecoderName = chooseDecoder(videoFormat);
                    MediaCodec codec = MediaCodec.createByCodecName(videoDecoderName);
                    codec.configure(videoFormat, null, null, 0);
                    MediaFormat outputFormat = codec.getOutputFormat(); // option B
                    codec.start();
                    while (!outputDone) {
                        //填充数据
                        if (!inputDone) {
                            int inputBufferId = codec.dequeueInputBuffer(0);//0：立即返回；<0：无限期等待，直到有可有的输入缓冲才返回；>0：有可用的缓存立即返回，没有则等待，超过指定时间会马上返回。
                            if (inputBufferId >= 0) {
                                ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferId);
                                // fill inputBuffer with valid data
                                int sampleSize = extractor.readSampleData(inputBuffer, 0);
                                if (sampleSize < 0) {
                                    //数据填充完毕
                                    codec.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                    inputDone = true;
                                } else {
                                    long sampleTime = extractor.getSampleTime();
                                    codec.queueInputBuffer(inputBufferId, 0, sampleSize, sampleTime, 0);
                                    extractor.advance();
                                }
                            }
                        }

                        //获取解码数据
                        int outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 0);
                        switch (outputBufferId) {
                            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                // Subsequent data will conform to new format.
                                // Can ignore if using getOutputFormat(outputBufferId)
                                outputFormat = codec.getOutputFormat(); // option B
                                Log.i(TAG, "run: new format: " + outputFormat);
                                break;

                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                //Log.i(TAG, "run: try later");
                                break;

                            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                Log.i(TAG, "run: output buffer changed");
                                break;

                            default:
                                if (outputBufferId >= 0) {
                                    ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);
                                    MediaFormat bufferFormat = codec.getOutputFormat(outputBufferId); // option A
                                    // bufferFormat is identical to outputFormat
                                    // outputBuffer is ready to be processed or rendered.
                                    fps.count();
                                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                        fps.show();
                                        Log.i(TAG, "output EOS");
                                        outputDone = true;
                                    }

                                    codec.releaseOutputBuffer(outputBufferId, true);
                                }
                                break;
                        }
                    }
                    codec.stop();
                    codec.release();
                }

                extractor.release();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void testParams(final AssetFileDescriptor afd) {
        new Thread(() -> {
            try {
                MediaExtractor extractor = new MediaExtractor();
                extractor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
                dumpFormat(extractor);
                MediaFormat videoFormat = chooseVideoTrack(extractor);
                Log.i(TAG, "choose decoder : " + chooseDecoder(videoFormat));
                extractor.release();
                displayDecoders();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void dumpFormat(MediaExtractor extractor) {
        int count = extractor.getTrackCount();
        Log.i(TAG, "playVideo: track count: " + count);
        for (int i = 0; i < count; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            Log.i(TAG, "playVideo: track " + i + ":" + getTrackInfo(format));
        }
    }

    private static String getTrackInfo(MediaFormat format) {
        String info = format.getString(MediaFormat.KEY_MIME);
        if (info.startsWith("audio/")) {
            info += " samplerate: " + format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    + ", channel count:" + format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        } else if (info.startsWith("video/")) {
            info += " size:" + format.getInteger(MediaFormat.KEY_WIDTH) + "x" + format.getInteger(MediaFormat.KEY_HEIGHT);
        }
        return info;
    }

    private static void displayDecoders() {
        MediaCodecList list = new MediaCodecList(MediaCodecList.REGULAR_CODECS);//REGULAR_CODECS参考api说明
        MediaCodecInfo[] codecs = list.getCodecInfos();
        for (MediaCodecInfo codec : codecs) {
            if (codec.isEncoder())
                continue;
            Log.i(TAG, "displayDecoders: " + codec.getName());
        }
    }

    private static MediaFormat chooseVideoTrack(MediaExtractor extractor) {
        int count = extractor.getTrackCount();
        for (int i = 0; i < count; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                extractor.selectTrack(i);//选择轨道
                return format;
            }
        }
        return null;
    }

    private static String chooseDecoder(MediaFormat format) {
        //fixme 应该复用MediaCodecList
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        return codecList.findDecoderForFormat(format);
    }
}
