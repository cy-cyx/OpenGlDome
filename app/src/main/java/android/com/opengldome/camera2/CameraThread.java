package android.com.opengldome.camera2;

import android.annotation.SuppressLint;
import android.com.opengldome.camera2.utils.CameraUtil;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static android.com.opengldome.camera2.Message.MSG_FOCUS;
import static android.com.opengldome.camera2.Message.MSG_PAUSE;
import static android.com.opengldome.camera2.Message.MSG_RELEASE;
import static android.com.opengldome.camera2.Message.MSG_RESUME;
import static android.com.opengldome.camera2.Message.MSG_SURFACE_CREATE;
import static android.com.opengldome.camera2.Message.MSG_SWITCH;
import static android.com.opengldome.camera2.Message.MSG_TAKEPIC;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_STATE_CONVERGED;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_STATE_FLASH_REQUIRED;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_INACTIVE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED;

/**
 * create by cy
 * time : 2019/11/29
 * version : 1.0
 * Features : 该线程中管理着{@link android.graphics.Camera}
 * xxxInner()方法说明运行相机专属的线程中
 * 1、点击聚焦测光 ✔
 * 2、镜头翻转 ✔
 * 3、拍照 ✔
 * 4、关于生命周期的处理 ✔
 */
public class CameraThread extends Thread {

    /**
     * 记住当前所有参数
     */
    public CameraConfig cameraConfig;

    private CameraManager cameraManager;
    private CameraThreadHandler cameraThreadHandler;
    private SurfaceTexture oesSurfaceTexture;

    // 相机的打开关闭 只有在生命周期，和镜头切换 如果为空说明没有准备好或者生命周期中 请求直接不处理
    private CameraDevice curCameraDevice;
    private CameraCaptureSession curCameraCaptureSession;

    private CameraThreadCallBack cameraThreadCallBack;
    private CameraCaptureSession.CaptureCallback captureCallback; // 相机拍摄结果的统一回调

    private boolean inOnPause = false;

    private @interface STATUS {
        int preview = 0;  // 预览模式（包括预览的对焦）
        int captureAf = 1; // 锁定焦点中。。。
        int captureAe = 2; // ae在执行预捕获
        int capture = 3;  // 拍照中
    }

    private int status = STATUS.preview;

    private ImageReader imageReader;
    private ImageReader.OnImageAvailableListener onImageAvailableListener;

    public CameraThread(Context context) {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        initListen();
    }

    private void initListen() {
        captureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                super.onCaptureStarted(session, request, timestamp, frameNumber);
            }

            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                super.onCaptureProgressed(session, request, partialResult);
                CameraUtil.logAF(partialResult);
                CameraUtil.logAE(partialResult);
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                CameraUtil.logAF(result);
                CameraUtil.logAE(result);
                // 拍照的情况下 （借鉴谷歌方法Dome）
                if (status == STATUS.captureAf) {
                    Object o = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (o == null) {
                        // 一些手机会为空
                        takePicInner();
                    } else {
                        int afAtatus = (int) o;
                        if (afAtatus == CONTROL_AF_STATE_FOCUSED_LOCKED ||
                                afAtatus == CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                            // 聚焦成功 开始预捕获
                            runPrecaptureSequenceInner();
                        } else if (afAtatus == CONTROL_AF_STATE_INACTIVE) {
                            // 部分手机前摄像头（mini）   CONTROL_AF_STATE_INACTIVE
                            // oppo 手机  预捕获 没有自动对焦  CONTROL_AF_STATE_PASSIVE_UNFOCUSED
                            takePicInner();
                        }
                    }
                } else if (status == STATUS.captureAe) {
                    Object o = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (o == null) {
                        takePicInner();
                        // 一些手机会为空
                    } else {
                        int aEAtatus = (int) o;
                        if (aEAtatus == CONTROL_AE_STATE_CONVERGED ||
                                aEAtatus == CONTROL_AE_STATE_FLASH_REQUIRED) {
                            takePicInner();
                        }
                    }
                }
            }

            @Override
            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
            }
        };
        onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                cameraThreadCallBack.onCapture(bytes, cameraConfig.cameraId.equals("1"), options.outWidth, options.outHeight);
            }
        };
    }

    /**
     * 获得对应镜头所有输出尺寸
     */
    public Size[] getOutSizeByCameraId(String id) {
        try {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streamConfigurationMap != null)
                return streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return new Size[0];
    }

    /**
     * 获得镜头完整的输出尺寸用的对焦
     */
    public Size getSensorPixelByCameraId(String id) {
        Size size = null;
        try {
            size = cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 找出最大合适的输出尺寸（opengl纹理有限制）
     */
    private Size getTakePicSizeByCamera(String id) {
        StreamConfigurationMap streamConfigurationMap = null;
        try {
            streamConfigurationMap = cameraManager.getCameraCharacteristics(id).
                    get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (streamConfigurationMap != null) {
            // 这里的输出不是顺序排序的（例如华为机型）
            Size[] outputSizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);

            // 从大到小排序
            List<Size> list = Arrays.asList(outputSizes);
            Comparator<Size> comparator = new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    if (o1.getWidth() < o2.getWidth()) {
                        return 1;
                    } else if (o1.getWidth() > o2.getWidth()) {
                        return -1;
                    }
                    return 0;
                }
            };
            Collections.sort(list, comparator);

            for (Size size : list) {
                if (cameraConfig.rotationIs90of270()) {
                    if (size.getWidth() <= cameraConfig.maxHeight && size.getHeight() <= cameraConfig.maxWidth) {
                        return size;
                    }
                } else {
                    if (size.getWidth() <= cameraConfig.maxWidth && size.getHeight() <= cameraConfig.maxHeight) {
                        return size;
                    }
                }
            }
            return outputSizes[0];
        }
        return new Size(cameraConfig.maxWidth, cameraConfig.maxHeight);
    }

    public void setCameraConfig(CameraConfig cameraConfig) {
        this.cameraConfig = cameraConfig;
    }

    @Override
    public void run() {
        Looper.prepare();
        synchronized (this) {
            cameraThreadHandler = new CameraThreadHandler(this);
            notifyAll();
        }
        Looper.loop();
    }

    private CameraThreadHandler getHandler() {
        synchronized (this) {
            while (cameraThreadHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return cameraThreadHandler;
        }
    }

    private void surfaceCreateInner(SurfaceTexture surfaceTexture) {
        oesSurfaceTexture = surfaceTexture;
        if (!inOnPause)
            openCameraInner();
    }

    @SuppressLint("MissingPermission")
    private void openCameraInner() {
        if (inOnPause) return;

        // 初始化一下拍照的（因为先要在管道中注册surface）
        Size size = getTakePicSizeByCamera(cameraConfig.cameraId);
        if (size == null) {
            throw new RuntimeException("相机错误");
        }
        cameraConfig.outSize = size;
        if (cameraConfig.rotationIs90of270()) {
            imageReader = ImageReader.newInstance(size.getHeight(), size.getWidth(), ImageFormat.JPEG, 2);
        } else {
            imageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, 2);
        }
        imageReader.setOnImageAvailableListener(onImageAvailableListener, getHandler());

        cameraThreadCallBack.onOpenCamera(cameraConfig.cameraId);
        try {
            cameraManager.openCamera(cameraConfig.cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    if (!inOnPause) {
                        curCameraDevice = camera;
                        createCaptureSessionInner();
                    }

                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }
            }, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        status = STATUS.preview;
    }

    private void onPauseInner() {
        inOnPause = true;
        closeCameraInner();
    }

    private void onResumeInner() {
        if (inOnPause) {
            inOnPause = false;
            openCameraInner();
        }
    }

    private void quitAndReleaseInner() {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            looper.quitSafely();
        }
    }

    private void switchCameraInner() {
        if (capturing() || inOnPause) return;
        cameraConfig.switchCamera();
        cameraConfig.resetAeAfMode();
        closeCameraInner();
        openCameraInner();
    }

    private void createCaptureSessionInner() {
        if (inOnPause || curCameraDevice == null) return;
        try {
            curCameraDevice.createCaptureSession(Arrays.<Surface>asList(new Surface(oesSurfaceTexture), imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (!inOnPause) {
                        curCameraCaptureSession = session;
                        // 管道连接后默认打开预览
                        openPreviewInner();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openPreviewInner() {
        CaptureRequest.Builder captureRequestBuilder = getCaptureRequestBuilderInner(CameraDevice.TEMPLATE_PREVIEW, new Surface(oesSurfaceTexture));
        if (captureRequestBuilder == null) return;
        RequestBuilderFactory.getRequestBuilderBase(captureRequestBuilder, cameraConfig);
        setRepeatingRequestInner(captureRequestBuilder.build());
    }

    private CaptureRequest.Builder getCaptureRequestBuilderInner(int templateType, Surface surfaceTexture) {
        if (inOnPause || curCameraDevice == null) return null;
        CaptureRequest.Builder captureRequest = null;
        try {
            captureRequest = curCameraDevice.createCaptureRequest(templateType);
            captureRequest.addTarget(surfaceTexture);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return captureRequest;
    }

    private void setRepeatingRequestInner(CaptureRequest captureRequest) {
        if (inOnPause || curCameraCaptureSession == null) return;
        try {
            curCameraCaptureSession.setRepeatingRequest(captureRequest, captureCallback, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureInner(CaptureRequest captureRequest) {
        if (inOnPause || curCameraCaptureSession == null) return;
        try {
            curCameraCaptureSession.capture(captureRequest, captureCallback, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCameraInner() {
        if (curCameraDevice != null)
            curCameraDevice.close();
        curCameraDevice = null;
        if (curCameraCaptureSession != null)
            curCameraCaptureSession.close();
        curCameraCaptureSession = null;
    }

    /**
     * @param x 在屏幕坐标的x
     * @param y 在屏幕坐标的y
     */
    private void focusAeAfInner(int x, int y) {
        if (capturing() || inOnPause) return;
        MeteringRectangle[] meteringRectangles = CameraUtil.focusAeAf(x, y,
                cameraConfig.optimalSize, getSensorPixelByCameraId(cameraConfig.cameraId),
                cameraConfig.rotation, cameraConfig.cameraId);

        cameraConfig.controlAfMode = CaptureRequest.CONTROL_AF_MODE_AUTO;
        cameraConfig.afRectangles = new MeteringRectangle[]{meteringRectangles[0]};
        cameraConfig.aeRectangles = new MeteringRectangle[]{meteringRectangles[1]};

        CaptureRequest.Builder captureRequestBuilder = getCaptureRequestBuilderInner(CameraDevice.TEMPLATE_PREVIEW, new Surface(oesSurfaceTexture));
        if (captureRequestBuilder == null) return;
        RequestBuilderFactory.getRequestBuilderBase(captureRequestBuilder, cameraConfig);
        setRepeatingRequestInner(captureRequestBuilder.build());

        // 开始对焦
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        captureInner(captureRequestBuilder.build());
    }

    private boolean capturing() {
        return status == STATUS.captureAf
                || status == STATUS.captureAe
                || status == STATUS.capture;
    }

    /**
     * 聚焦 - 》 预捕获序列（AE）{@link #runPrecaptureSequenceInner()}- 》拍照{@link #takePicInner()}
     */
    private void focusToTakePicInner() {
        if (inOnPause || capturing()) return;
        CaptureRequest.Builder captureRequestBuilder = getCaptureRequestBuilderInner(CameraDevice.TEMPLATE_PREVIEW, new Surface(oesSurfaceTexture));
        if (captureRequestBuilder == null) return;
        RequestBuilderFactory.getRequestBuilderBase(captureRequestBuilder, cameraConfig);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        captureInner(captureRequestBuilder.build());
        status = STATUS.captureAf;
    }


    private void runPrecaptureSequenceInner() {
        if (inOnPause) return;
        CaptureRequest.Builder captureRequestBuilder = getCaptureRequestBuilderInner(CameraDevice.TEMPLATE_PREVIEW, new Surface(oesSurfaceTexture));
        if (captureRequestBuilder == null) return;
        RequestBuilderFactory.getRequestBuilderBase(captureRequestBuilder, cameraConfig);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        captureInner(captureRequestBuilder.build());
        status = STATUS.captureAe;
    }

    private void takePicInner() {
        if (inOnPause) return;
        CaptureRequest.Builder captureRequestBuilder = getCaptureRequestBuilderInner(CameraDevice.TEMPLATE_STILL_CAPTURE, imageReader.getSurface());
        if (captureRequestBuilder == null) return;
        captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                cameraConfig.cameraId.equals("1") ? cameraConfig.rotation + 180 : cameraConfig.rotation);
        try {
            curCameraCaptureSession.stopRepeating();
            curCameraCaptureSession.abortCaptures();
        } catch (CameraAccessException e) {
            // nodo
        }
        captureInner(captureRequestBuilder.build());
        status = STATUS.capture;
    }

    private static class CameraThreadHandler extends Handler {

        private WeakReference<CameraThread> cameraThreadWeakReference;

        private CameraThreadHandler(CameraThread cameraThread) {
            cameraThreadWeakReference = new WeakReference<>(cameraThread);
        }

        @Override
        public void handleMessage(Message msg) {
            CameraThread cameraThread;
            switch (msg.what) {
                case MSG_SURFACE_CREATE:
                    cameraThread = cameraThreadWeakReference.get();
                    if (cameraThread != null) {
                        SurfaceTexture surfaceTexture = (SurfaceTexture) msg.obj;
                        cameraThread.surfaceCreateInner(surfaceTexture);
                    }
                    break;
                case MSG_PAUSE:
                    cameraThread = cameraThreadWeakReference.get();
                    if (cameraThread != null) {
                        cameraThread.onPauseInner();
                    }
                    break;
                case MSG_RESUME:
                    cameraThread = cameraThreadWeakReference.get();
                    if (cameraThread != null) {
                        cameraThread.onResumeInner();
                    }
                    break;
                case MSG_RELEASE:
                    cameraThread = cameraThreadWeakReference.get();
                    if (cameraThread != null) {
                        cameraThread.quitAndReleaseInner();
                    }
                    break;
                case MSG_SWITCH:
                    cameraThread = cameraThreadWeakReference.get();
                    if (cameraThread != null) {
                        cameraThread.switchCameraInner();
                    }
                    break;
                case MSG_FOCUS:
                    HashMap request = (HashMap) msg.obj;
                    Object x = request.get("x");
                    Object y = request.get("y");
                    int viewClickX = 0;
                    int viewClickY = 0;
                    if (x instanceof Integer)
                        viewClickX = (int) x;
                    if (y instanceof Integer)
                        viewClickY = (int) y;

                    cameraThread = cameraThreadWeakReference.get();
                    if (cameraThread != null) {
                        cameraThread.focusAeAfInner(viewClickX, viewClickY);
                    }
                    break;
                case MSG_TAKEPIC:
                    cameraThread = cameraThreadWeakReference.get();
                    if (cameraThread != null) {
                        cameraThread.focusToTakePicInner();
                    }
                    break;
            }
        }

    }

    public void surfaceCreate(SurfaceTexture surfaceTexture) {
        if (getHandler() != null)
            Message.obtain(getHandler(), MSG_SURFACE_CREATE, surfaceTexture).sendToTarget();
    }

    public void onPause() {
        if (getHandler() != null)
            Message.obtain(getHandler(), MSG_PAUSE).sendToTarget();
    }

    public void onResume() {
        if (getHandler() != null)
            Message.obtain(getHandler(), MSG_RESUME).sendToTarget();
    }

    public void release() {
        if (getHandler() != null)
            Message.obtain(getHandler(), MSG_RELEASE).sendToTarget();
    }

    public void switchCamera() {
        if (getHandler() != null)
            Message.obtain(getHandler(), MSG_SWITCH).sendToTarget();
    }

    public void focusAEAF(int x, int y) {
        HashMap<String, Integer> request = new HashMap<>();
        request.put("x", x);
        request.put("y", y);
        if (getHandler() != null)
            Message.obtain(getHandler(), MSG_FOCUS, request).sendToTarget();
    }

    public void takePic() {
        if (getHandler() != null)
            Message.obtain(getHandler(), MSG_TAKEPIC, null).sendToTarget();
    }

    public void setCameraThreadCallBack(CameraThreadCallBack callBack) {
        this.cameraThreadCallBack = callBack;
    }

    public interface CameraThreadCallBack {
        public void onOpenCamera(String id);

        /**
         * 拍照的回调
         *
         * @param data   图像数据
         * @param front  前置需要颠倒
         * @param width  宽
         * @param height 高
         */
        public void onCapture(byte[] data, boolean front, int width, int height);
    }
}
