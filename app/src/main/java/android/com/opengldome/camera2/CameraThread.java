package android.com.opengldome.camera2;

import android.annotation.SuppressLint;
import android.com.opengldome.camera2.utils.CameraUtil;
import android.content.Context;
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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;

import static android.com.opengldome.camera2.Message.MSG_FOCUS;
import static android.com.opengldome.camera2.Message.MSG_PAUSE;
import static android.com.opengldome.camera2.Message.MSG_RELEASE;
import static android.com.opengldome.camera2.Message.MSG_RESUME;
import static android.com.opengldome.camera2.Message.MSG_SURFACE_CREATE;
import static android.com.opengldome.camera2.Message.MSG_SWITCH;

/**
 * create by cy
 * time : 2019/11/29
 * version : 1.0
 * Features : 该线程中管理着{@link android.graphics.Camera}
 * xxxInner()方法说明运行相机专属的线程中
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
                CameraUtil.logFocus(partialResult);
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                CameraUtil.logFocus(result);
            }

            @Override
            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
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

    public Size getSensorPixelByCameraId(String id) {
        Size size = null;
        try {
            size = cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return size;
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
        cameraConfig.switchCamera();
        cameraConfig.resetAeAfMode();
        closeCameraInner();
        openCameraInner();
    }

    private void createCaptureSessionInner() {
        if (inOnPause || curCameraDevice == null) return;
        try {
            curCameraDevice.createCaptureSession(Arrays.<Surface>asList(new Surface(oesSurfaceTexture)), new CameraCaptureSession.StateCallback() {
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
        CaptureRequest.Builder captureRequestBuilder = getCaptureRequestBuilderInner(new Surface(oesSurfaceTexture));
        if (captureRequestBuilder == null) return;
        RequestBuilderFactory.getRequestBuilderBase(captureRequestBuilder, cameraConfig);
        setRepeatingRequestInner(captureRequestBuilder.build());
    }

    private CaptureRequest.Builder getCaptureRequestBuilderInner(Surface surfaceTexture) {
        if (inOnPause || curCameraDevice == null) return null;
        CaptureRequest.Builder captureRequest = null;
        try {
            captureRequest = curCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
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
            curCameraCaptureSession.capture(captureRequest, null, new Handler());
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
        MeteringRectangle[] meteringRectangles = CameraUtil.focusAeAf(x, y,
                cameraConfig.optimalSize, getSensorPixelByCameraId(cameraConfig.cameraId),
                cameraConfig.rotation, cameraConfig.cameraId);

        cameraConfig.controlAfMode = CaptureRequest.CONTROL_AF_MODE_AUTO;
        cameraConfig.afRectangles = new MeteringRectangle[]{meteringRectangles[0]};
        cameraConfig.aeRectangles = new MeteringRectangle[]{meteringRectangles[1]};

        CaptureRequest.Builder captureRequestBuilder = getCaptureRequestBuilderInner(new Surface(oesSurfaceTexture));
        if (captureRequestBuilder == null) return;
        RequestBuilderFactory.getRequestBuilderBase(captureRequestBuilder, cameraConfig);
        setRepeatingRequestInner(captureRequestBuilder.build());

        // 开始对焦
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        captureInner(captureRequestBuilder.build());
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

    public void setCameraThreadCallBack(CameraThreadCallBack callBack) {
        this.cameraThreadCallBack = callBack;
    }

    public interface CameraThreadCallBack {
        public void onOpenCamera(String id);
    }
}
