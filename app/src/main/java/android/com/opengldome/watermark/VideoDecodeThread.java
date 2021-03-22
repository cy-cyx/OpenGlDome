package android.com.opengldome.watermark;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.com.opengldome.watermark.WaterMarkProcess.OAS_AVAILABLE;
import static android.com.opengldome.watermark.WaterMarkProcess.VIDEO_DECODE_FINISH;
import static android.media.MediaFormat.KEY_MIME;
import static android.media.MediaFormat.KEY_ROTATION;

class VideoDecodeThread implements Runnable {

    private WaterMarkProcess.WaterMarkHandler waterMarkHandler;
    private String videoPath;
    private SurfaceTexture targetSurfaceTexture;

    volatile AtomicBoolean consume = new AtomicBoolean(false);

    VideoDecodeThread(WaterMarkProcess.WaterMarkHandler handler, String path, SurfaceTexture oes) {
        waterMarkHandler = handler;
        videoPath = path;
        targetSurfaceTexture = oes;
    }

    @Override
    public void run() {
        Log.d("xx", "开始视频解码");

        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 找出视频轨道
        int videoTrack = 0;
        MediaFormat videoMediaFormat = null;

        int trackCount = mediaExtractor.getTrackCount();
        for (int curTrack = 0; curTrack < trackCount; curTrack++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(curTrack);
            String mime = trackFormat.getString(KEY_MIME);
            if (mime.startsWith("video/")) {

                videoTrack = curTrack;
                videoMediaFormat = trackFormat;
                break;
            }
        }
        // 选定轨道
        mediaExtractor.selectTrack(videoTrack);

        MediaCodec mDecodeMediaCodec = null;

        // 构建解码器
        try {
            mDecodeMediaCodec = MediaCodec.createDecoderByType(videoMediaFormat.getString(KEY_MIME));
            mDecodeMediaCodec.configure(videoMediaFormat, new Surface(targetSurfaceTexture), null, 0);
            mDecodeMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 开始解码
        boolean decodeInputDone = false;
        boolean decodeOutputDone = false;

        while (true) {
            if (!decodeInputDone) {
                // 寻找可以输入缓冲区
                int inputBufIndex = mDecodeMediaCodec.dequeueInputBuffer(1000);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuffer = mDecodeMediaCodec.getInputBuffer(inputBufIndex);
                    // 读数据
                    int i = mediaExtractor.readSampleData(inputBuffer, 0);
                    if (i < 0) {
                        decodeInputDone = true;
                        mDecodeMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        mDecodeMediaCodec.queueInputBuffer(inputBufIndex, 0, i, mediaExtractor.getSampleTime(), 0);
                        mediaExtractor.advance();
                    }
                }
            }

            MediaCodec.BufferInfo decodeOutputInfo = new MediaCodec.BufferInfo();
            if (!decodeOutputDone && !consume.get()/*等待当前帧被加好水印消化*/) {
                int decoderStatus = mDecodeMediaCodec.dequeueOutputBuffer(decodeOutputInfo, 1000);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // 呼叫超时
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // 输出格式已更改
                    MediaFormat mediaFormat = mDecodeMediaCodec.getOutputFormat();
                    if (videoMediaFormat.containsKey(KEY_ROTATION)) {
                        mediaFormat.setInteger(KEY_ROTATION, videoMediaFormat.getInteger(KEY_ROTATION));
                    } else {
                        mediaFormat.setInteger(KEY_ROTATION, 0);
                    }
                    Message.obtain(waterMarkHandler, WaterMarkProcess.VIDEO_FORMAT, mediaFormat).sendToTarget();
                } else if (decoderStatus < 0) {
                    // 没有可用输出
                } else {

                    ByteBuffer outputBuffer = mDecodeMediaCodec.getOutputBuffer(decoderStatus);
                    outputBuffer.position(decodeOutputInfo.offset);
                    outputBuffer.limit(decodeOutputInfo.offset + decodeOutputInfo.size);
                    byte[] decodeData = new byte[decodeOutputInfo.size];
                    outputBuffer.get(decodeData);

                    mDecodeMediaCodec.releaseOutputBuffer(decoderStatus, true);

                    if ((decodeOutputInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        decodeOutputDone = true;
                    } else {
                        consume.set(true);
                        Message.obtain(waterMarkHandler, OAS_AVAILABLE, decodeOutputInfo.presentationTimeUs).sendToTarget();
                    }
                }

                // 数据解完结束线程
                if (decodeInputDone && decodeOutputDone) {
                    Message.obtain(waterMarkHandler, VIDEO_DECODE_FINISH).sendToTarget();
                    break;
                }
            }

        }

        mediaExtractor.release();
        mDecodeMediaCodec.stop();
        mDecodeMediaCodec.release();

        Log.d("xx", "视频解码结束");
    }
}
