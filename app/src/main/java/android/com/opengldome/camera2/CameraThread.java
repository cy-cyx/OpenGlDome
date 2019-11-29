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
 * Features :
 */
public class CameraThread extends Thread {

    private static final int MSG_SURFACE_CREATE = 0;
    public static final int MSG_OPEN_CAMERA = 1;

    private CameraManager cameraManager;
    private CameraThreadHandler cameraThreadHandler;
    private SurfaceTexture oesSurfaceTexture;
    private CameraDevice cameraDevice;

    public CameraThread(Context context) {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
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
        openCameraInner();
    }

    @SuppressLint("MissingPermission")
    private void openCameraInner() {
        try {
            cameraManager.openCamera("1", new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startPreviewInner();
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

    private void startPreviewInner() {
        CaptureRequest.Builder previewRequestBuilder = null;
        try {
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        previewRequestBuilder.addTarget(new Surface(oesSurfaceTexture));
        try {
            final CaptureRequest.Builder finalPreviewRequestBuilder = previewRequestBuilder;
            cameraDevice.createCaptureSession(Arrays.<Surface>asList(new Surface(oesSurfaceTexture)), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        // 自动对焦
                        finalPreviewRequestBuilder.set(CaptureRequest.BLACK_LEVEL_LOCK, false);
                        finalPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打开闪光灯
                        finalPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE);
                        finalPreviewRequestBuilder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_ON);
                        // 显示预览
                        CaptureRequest previewRequest = finalPreviewRequestBuilder.build();
                        cameraCaptureSession.setRepeatingRequest(previewRequest, null, new Handler());
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void surfaceCreate(SurfaceTexture surfaceTexture) {
        if (getHandler() != null)
            Message.obtain(getHandler(), MSG_SURFACE_CREATE, surfaceTexture).sendToTarget();
    }

    private static class CameraThreadHandler extends Handler {

        WeakReference<CameraThread> cameraThreadWeakReference;

        public CameraThreadHandler(CameraThread cameraThread) {
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
                case MSG_OPEN_CAMERA:
                    break;
            }

        }
    }
}
