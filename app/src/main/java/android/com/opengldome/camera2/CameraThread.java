package android.com.opengldome.camera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * create by cy
 * time : 2019/11/29
 * version : 1.0
 * Features : 该线程中管理着{@link android.graphics.Camera}
 */
public class CameraThread extends Thread {

    private CameraConfig cameraConfig;

    private static final int MSG_SURFACE_CREATE = 0; // 预览GlSurfaceView的OES成功创建（正常只会调用一次）
    public static final int MSG_PAUSE = 1;
    public static final int MSG_RESUME = 2;
    public static final int MSG_RELEASE = 3;

    private CameraManager cameraManager;
    private CameraThreadHandler cameraThreadHandler;
    private SurfaceTexture oesSurfaceTexture;

    private CameraDevice curCameraDevice;
    private CameraCaptureSession curCameraCaptureSession;

    private boolean inOnPause = false;

    public CameraThread(Context context) {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
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

    private void onPauseInner() {
        inOnPause = true;
        if (curCameraDevice != null)
            curCameraDevice.close();
        curCameraDevice = null;
        curCameraCaptureSession = null;
    }

    private void onResumeInner() {
        if (inOnPause) {
            openCameraInner();
        }
        inOnPause = false;
    }

    private void quitAndReleaseInner() {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            looper.quitSafely();
        }
    }

    @SuppressLint("MissingPermission")
    private void openCameraInner() {
        try {
            cameraManager.openCamera(cameraConfig.cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    if (!inOnPause) {
                        curCameraDevice = camera;
                        createCaptureSession();
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

    private void createCaptureSession() {
        if (curCameraDevice == null) return;
        try {
            curCameraDevice.createCaptureSession(Arrays.<Surface>asList(new Surface(oesSurfaceTexture)), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (!inOnPause) {
                        curCameraCaptureSession = session;
                        // 管道连接后默认打开预览
                        openAndAdjustPreview();
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

    private void openAndAdjustPreview() {
        if (!inOnPause && curCameraDevice != null && curCameraCaptureSession != null) {
            try {
                CaptureRequest.Builder previewRequestBuilder = curCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewRequestBuilder.addTarget(new Surface(oesSurfaceTexture));
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, cameraConfig.controlAfMode);
                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, cameraConfig.controlAeMode);
                curCameraCaptureSession.setRepeatingRequest(previewRequestBuilder.build(), null, new Handler());
            } catch (CameraAccessException e) {
                e.printStackTrace();
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
            }
        }
    }
}
