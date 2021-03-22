package android.com.opengldome.watermark;

import android.com.opengldome.utils.FileUtils;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * create by caiyx in 2021/3/19
 * <p>
 * 混合器
 */
class MediaMuxerProxy {

    private MediaMuxer mediaMuxer;

    MediaMuxerProxy() {
        try {
            mediaMuxer = new MediaMuxer(FileUtils.getNewMp4Path(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setOrientationHint(int degrees) {
        mediaMuxer.setOrientationHint(degrees);
    }

    private boolean videoTrack = false;

    int addVideoTrack(MediaFormat format) {
        videoTrack = true;
        int track = mediaMuxer.addTrack(format);
        notifyAll();
        return track;
    }

    private boolean audioTrack = false;

    int addAudioTrack(MediaFormat format) {
        audioTrack = true;
        int track = mediaMuxer.addTrack(format);
        notifyAll();
        return track;
    }

    boolean canStart() {
        return videoTrack && audioTrack;
    }


    public void start() {
        mediaMuxer.start();
    }

    void stopVideo() {
        videoTrack = false;
    }

    void stopAudio() {
        audioTrack = false;
    }

    boolean canStop() {
        return !(videoTrack || audioTrack);
    }

    void stop() {
        mediaMuxer.stop();
    }

    void release() {
        mediaMuxer.release();
    }

    synchronized void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        mediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }
}
