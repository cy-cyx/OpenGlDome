package android.com.opengldome.camera2;

import android.com.opengldome.camera2.view.ControlLayout;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * create by cy
 * time : 2019/11/28
 * version : 1.0
 * Features : 用外部纹理预览拍照
 */
public class Camera2Activity extends AppCompatActivity {

    private CameraGLTextureView glTextureView;
    private Camera2Render camera2Render;
    private CameraThread cameraThread;
    private ControlLayout controlLayout;

    private Camera2Render.Camera2RenderCallBack camera2RenderCallBack;
    private ControlLayout.ControlLayoutCallback controlLayoutCallback;
    private CameraThread.CameraThreadCallBack cameraThreadCallBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListen();
        initCamera();
        initView();
    }

    private void initListen() {
        camera2RenderCallBack = new Camera2Render.Camera2RenderCallBack() {
            @Override
            public void onEOSAvailable(SurfaceTexture surfaceTexture) {
                cameraThread.surfaceCreate(surfaceTexture);
            }
        };
        controlLayoutCallback = new ControlLayout.ControlLayoutCallback() {
            @Override
            public void onSwitch() {
                cameraThread.switchCamera();
            }
        };
        cameraThreadCallBack = new CameraThread.CameraThreadCallBack() {
            @Override
            public void onOpenCamera(String cameraId) {
                camera2Render.setCameraId(cameraId);
            }
        };
    }

    private void initCamera() {
        CameraConfig cameraConfig = new CameraConfig();

        cameraThread = new CameraThread(Camera2Activity.this);
        cameraThread.setCameraConfig(cameraConfig);
        cameraThread.setCameraThreadCallBack(cameraThreadCallBack);
        cameraThread.start();
    }

    private void initView() {
        FrameLayout layout = new FrameLayout(this);
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(layout, vl);

        initPreviewView(layout);

        controlLayout = new ControlLayout(this);
        controlLayout.setControlLayoutCallback(controlLayoutCallback);
        vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(controlLayout, vl);
    }

    private void initPreviewView(ViewGroup viewGroup) {
        glTextureView = new CameraGLTextureView(this);
        camera2Render = new Camera2Render();
        camera2Render.setCamera2RenderCallBack(camera2RenderCallBack);
        glTextureView.setRender(camera2Render);
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewGroup.addView(glTextureView, vl);
    }

    @Override
    protected void onPause() {
        cameraThread.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraThread.onResume();
    }

    @Override
    protected void onDestroy() {
        cameraThread.release();
        super.onDestroy();
    }
}
