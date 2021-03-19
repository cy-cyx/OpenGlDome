package android.com.opengldome.watermark;

import android.com.opengldome.utils.FileUtils;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * create by caiyx in 2021/3/19
 * <p>
 * 混合器
 */
public class MediaMuxerProxy {

    private MediaMuxer mediaMuxer;

    public MediaMuxerProxy() {
        try {
            mediaMuxer = new MediaMuxer(FileUtils.getNewMp4Path(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTrack(MediaFormat format) {
        mediaMuxer.addTrack(format);
    }

    public void start(){
        mediaMuxer.start();
    }

    public synchronized void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        mediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }
}
