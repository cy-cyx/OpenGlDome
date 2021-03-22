package android.com.opengldome.watermark;

import android.media.MediaCodec;
import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.LinkedList;

public class VideoEncodeThread implements Runnable {

    private MediaCodec mediaCodec;
    private final MediaMuxerProxy mediaMuxerProxy;
    private WaterMarkProcess.WaterMarkHandler waterMarkHandler;

    VideoEncodeThread(MediaCodec encode, MediaMuxerProxy proxy, WaterMarkProcess.WaterMarkHandler handler) {
        mediaCodec = encode;
        mediaMuxerProxy = proxy;
        waterMarkHandler = handler;
    }

    @Override
    public void run() {
        Log.d("xx", "开始编码线程");
        MediaCodec.BufferInfo encodeOutputInfo = new MediaCodec.BufferInfo();
        int muxerTrack = 0;
        while (true) {
            int decoderStatus = mediaCodec.dequeueOutputBuffer(encodeOutputInfo, 1000);
            if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // 呼叫超时
            } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // 输出格式已更改

                // 需要等待音频
                synchronized (mediaMuxerProxy) {
                    muxerTrack = mediaMuxerProxy.addVideoTrack(mediaCodec.getOutputFormat());
                    if (mediaMuxerProxy.canStart()) {
                        mediaMuxerProxy.start();
                    }
                    while (!mediaMuxerProxy.canStart()) {
                        try {
                            mediaMuxerProxy.wait();
                        } catch (InterruptedException e) {

                        }
                    }
                }

            } else if (decoderStatus < 0) {
                // 没有可用输出
            } else {

                if ((encodeOutputInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }

                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(decoderStatus);

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                bufferInfo.size = encodeOutputInfo.size;
                bufferInfo.offset = encodeOutputInfo.offset;
                bufferInfo.flags = encodeOutputInfo.flags;
                bufferInfo.presentationTimeUs = encodeOutputInfo.presentationTimeUs;

                mediaMuxerProxy.writeSampleData(muxerTrack, outputBuffer, bufferInfo);
                mediaCodec.releaseOutputBuffer(decoderStatus, false);
            }

        }

        synchronized (mediaMuxerProxy) {
            mediaMuxerProxy.stopVideo();

            if (mediaMuxerProxy.canStop()) {
                mediaMuxerProxy.stop();
                mediaMuxerProxy.release();
                Message.obtain(waterMarkHandler, WaterMarkProcess.ALL_ENCODE_FINISH).sendToTarget();
            }
        }
        Log.d("xx", "编码线程结束");
    }
}
