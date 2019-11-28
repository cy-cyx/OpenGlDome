//package android.com.opengldome.camera2;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CaptureRequest;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.Arrays;
//
//public class MainActivity extends AppCompatActivity {
//
//    SurfaceView surfaceView;
//    SurfaceHolder surfaceHolder;
//    CameraManager cameraManager;
//    CameraDevice cameraDevice;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        surfaceView = new SurfaceView(this);
//        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        addContentView(surfaceView, vl);
//        surfaceHolder = surfaceView.getHolder();
//        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder surfaceHolder) {
//                initCamera();
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//
//            }
//        });
//    }
//
//    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(@NonNull CameraDevice cameraDevice) {
//            MainActivity.this.cameraDevice = cameraDevice;
//            takePreview();
//        }
//
//        @Override
//        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
//
//        }
//
//        @Override
//        public void onError(@NonNull CameraDevice cameraDevice, int i) {
//
//        }
//    };
//
//    public void initCamera() {
//        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//            cameraManager.openCamera("1", stateCallback, new Handler());
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                cameraDevice.close();
//                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//                try {
//                    cameraManager.openCamera("0", stateCallback, new Handler());
//                } catch (CameraAccessException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 2000);
//    }
//
//    public void takePreview() {
//        CaptureRequest.Builder previewRequestBuilder = null;
//        try {
//            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        previewRequestBuilder.addTarget(surfaceHolder.getSurface());
//        try {
//            final CaptureRequest.Builder finalPreviewRequestBuilder = previewRequestBuilder;
//            cameraDevice.createCaptureSession(Arrays.<Surface>asList(surfaceHolder.getSurface()), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    try {
//                        // 自动对焦
//                        finalPreviewRequestBuilder.set(CaptureRequest.BLACK_LEVEL_LOCK, false);
//                        finalPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                        // 打开闪光灯
//                        finalPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE);
//                        finalPreviewRequestBuilder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_ON);
//                        // 显示预览
//                        CaptureRequest previewRequest = finalPreviewRequestBuilder.build();
//                        cameraCaptureSession.setRepeatingRequest(previewRequest, null, new Handler());
//                    } catch (CameraAccessException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//
//                }
//            }, new Handler());
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//}
