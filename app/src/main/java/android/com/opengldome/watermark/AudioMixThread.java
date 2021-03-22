package android.com.opengldome.watermark;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaFormat.KEY_MIME;

/**
 * create by caiyx in 2021/3/22
 * <p>
 * 把音频加回去
 */
public class AudioMixThread implements Runnable {

    private WaterMarkProcess.WaterMarkHandler waterMarkHandler;
    private String audioPath;
    private final MediaMuxerProxy mediaMuxerProxy;

    public AudioMixThread(WaterMarkProcess.WaterMarkHandler handler, String path, MediaMuxerProxy muxer) {
        waterMarkHandler = handler;
        audioPath = path;
        mediaMuxerProxy = muxer;
    }

    @Override
    public void run() {
        Log.d("xx", "开始音频整合线程");

        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(audioPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 找出视频轨道
        int audioTrack = 0;
        MediaFormat audioMediaFormat = null;

        int trackCount = mediaExtractor.getTrackCount();
        for (int curTrack = 0; curTrack < trackCount; curTrack++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(curTrack);
            String mime = trackFormat.getString(KEY_MIME);
            if (mime.startsWith("audio/")) {

                audioTrack = curTrack;
                audioMediaFormat = trackFormat;
                break;
            }
        }
        // 选定轨道
        mediaExtractor.selectTrack(audioTrack);

        int track;

        // 需要等待视频
        synchronized (mediaMuxerProxy) {
            track = mediaMuxerProxy.addAudioTrack(audioMediaFormat);
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


        while (true) {

            ByteBuffer buffer = ByteBuffer.allocateDirect(1000 * 1000);
            int size = mediaExtractor.readSampleData(buffer, 0);
            long time = mediaExtractor.getSampleTime();
            int flags = mediaExtractor.getSampleFlags();
            if (size < 0) {
                break;
            } else {

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                bufferInfo.size = size;
                bufferInfo.offset = 0;
                bufferInfo.flags = flags;
                bufferInfo.presentationTimeUs = time;

                mediaMuxerProxy.writeSampleData(track, buffer, bufferInfo);
                mediaExtractor.advance();
            }
        }

        synchronized (mediaMuxerProxy){
            mediaMuxerProxy.stopAudio();

            if (mediaMuxerProxy.canStop()){
                mediaMuxerProxy.stop();
                mediaMuxerProxy.release();
                Message.obtain(waterMarkHandler, WaterMarkProcess.ALL_ENCODE_FINISH).sendToTarget();
            }
        }


        Log.d("xx", "音频整合线程完成");
    }
}
