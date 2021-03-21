package android.com.opengldome.watermark;

import android.media.MediaCodec;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.LinkedList;

public class VideoEncodeThread implements Runnable {

    private MediaCodec mediaCodec;
    private MediaMuxerProxy mediaMuxerProxy;

    VideoEncodeThread(MediaCodec encode, MediaMuxerProxy proxy) {
        mediaCodec = encode;
        mediaMuxerProxy = proxy;
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
                muxerTrack = mediaMuxerProxy.addVideoTrack(mediaCodec.getOutputFormat());
                mediaMuxerProxy.start();
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
        mediaMuxerProxy.stop();
        mediaMuxerProxy.release();
        Log.d("xx", "编码线程结束");
    }
}
