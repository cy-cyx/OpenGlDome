package android.com.opengldome.watermark;

import android.com.opengldome.egl.EGLHelp;
import android.com.opengldome.utils.CommonUtils;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGLExt;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import static android.media.MediaFormat.KEY_FRAME_RATE;
import static android.media.MediaFormat.KEY_I_FRAME_INTERVAL;
import static android.media.MediaFormat.KEY_LEVEL;
import static android.media.MediaFormat.KEY_ROTATION;

/**
 * create by caiyx in 2021/3/19
 * <p>
 * 添加视频水印的主线程
 */
public class WaterMarkProcess implements Runnable {

    private WaterMarkProcessCallBack waterMarkProcessCallBack;

    private String videoPath;
    private WaterMarkHandler waterMarkHandler;

    private EGLHelp eglHelp;
    private WaterMarkRender render;

    // 获得解码数据的oes
    private int oesTexture;
    private SurfaceTexture oesSurfaceTexture;

    // 视频解码
    private VideoDecodeThread videoDecodeThread;

    // 视频编码
    private VideoEncodeThread videoEncodeThread;
    private MediaCodec encoder;

    // 混合器
    private MediaMuxerProxy mediaMuxerProxy;

    // 音频整合线程
    private AudioMixThread audioMixThread;

    WaterMarkProcess(String path, WaterMarkProcessCallBack callBack) {
        videoPath = path;
        waterMarkProcessCallBack = callBack;
    }

    @Override
    public void run() {
        Log.d("xx", "开始加水印线程");
        Looper.prepare();

        waterMarkHandler = new WaterMarkHandler();

        initGl();

        // 视频解码
        videoDecodeThread = new VideoDecodeThread(waterMarkHandler, videoPath, oesSurfaceTexture);
        new Thread(videoDecodeThread).start();

        // 构建混合器
        mediaMuxerProxy = new MediaMuxerProxy();

        // 音频整合
        audioMixThread = new AudioMixThread(waterMarkHandler, videoPath, mediaMuxerProxy);
        new Thread(audioMixThread).start();

        Looper.loop();
        Log.d("xx", "加水印线程结束");
    }

    private void initGl() {
        Log.d("xx", "初始化egl环境");
        // 初始化egl环境
        eglHelp = new EGLHelp();
        eglHelp.start();

        // oes
        oesTexture = CommonUtils.createTextureOES();
        oesSurfaceTexture = new SurfaceTexture(oesTexture);

        // 初始化render
        render = new WaterMarkRender();
        render.onSurfaceCreate();

    }

    // 解码器返回视频参数
    private void initFormat(MediaFormat format) {
        // 设置方向
        int rotation = format.getInteger(KEY_ROTATION);
        mediaMuxerProxy.setOrientationHint(rotation);

        // 构建编码器,并启动编码线程
        startEncode(format);

        render.onSurfaceChange(width, height, rotation);
    }

    private int width;
    private int height;

    // 绑定编码器和启动编码线程
    private void startEncode(MediaFormat format) {
        try {
            width = format.getInteger(MediaFormat.KEY_WIDTH);
            if (format.containsKey("crop-left") && format.containsKey("crop-right")) {
                width = format.getInteger("crop-right") + 1 - format.getInteger("crop-left");
            }
            height = format.getInteger(MediaFormat.KEY_HEIGHT);
            if (format.containsKey("crop-top") && format.containsKey("crop-bottom")) {
                height = format.getInteger("crop-bottom") + 1 - format.getInteger("crop-top");
            }

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(KEY_FRAME_RATE, 20);
            videoFormat.setInteger(KEY_I_FRAME_INTERVAL, 1);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 30);
            videoFormat.setInteger(KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
            videoFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
            encoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            // 绑定gl输出
            eglHelp.createSurface(encoder.createInputSurface());

            encoder.start();

            // 启动编码线程
            videoEncodeThread = new VideoEncodeThread(encoder, mediaMuxerProxy, waterMarkHandler);
            new Thread(videoEncodeThread).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 解码线程返回帧数据
    private void renderFlame(long time) {
        oesSurfaceTexture.updateTexImage();

        // 添加水印
        render.onDraw(oesTexture, width, height);

        // 塞入解码器
        /*EglWindowSurface*/
        EGLExt.eglPresentationTimeANDROID(eglHelp.getmEglDisplay(), eglHelp.getmEglSurface(), time * 1000);
        eglHelp.swap(); // 调用eglSwapBuffers（）会导致发送一帧数据到视频编码器

        // 通知解码可以解下一帧
        videoDecodeThread.consume.set(false);
    }

    // 通知解码结束，没有新的需要加水印的帧了
    private void videoDecodeFinish() {
        encoder.signalEndOfInputStream();

        // 停止绘画线程
        eglHelp.destroySurface();
        eglHelp.finish();
    }

    private void finishProcess() {

        waterMarkProcessCallBack.onFinish();

        Looper looper = Looper.myLooper();
        if (looper != null) {
            looper.quitSafely();
        }
    }

    static final int VIDEO_FORMAT = 1;
    static final int OAS_AVAILABLE = 2;
    static final int VIDEO_DECODE_FINISH = 3;
    static final int ALL_ENCODE_FINISH = 4;

    class WaterMarkHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case VIDEO_FORMAT:
                    initFormat((MediaFormat) msg.obj);
                    break;
                case OAS_AVAILABLE:
                    renderFlame((long) msg.obj);
                    break;
                case VIDEO_DECODE_FINISH:
                    videoDecodeFinish();
                    break;
                case ALL_ENCODE_FINISH:
                    finishProcess();
                    break;
            }
        }
    }

    interface WaterMarkProcessCallBack {
        void onFinish();
    }
}
