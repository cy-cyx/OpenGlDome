package android.com.opengldome.camera2;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.ViewGroup;

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
    private Camera2Render.Camera2RenderCallBack camera2RenderCallBack;
    private CameraThread cameraThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListen();
        initView();
    }

    private void initListen() {
        camera2RenderCallBack = new Camera2Render.Camera2RenderCallBack() {
            @Override
            public void onEOSAvailable(SurfaceTexture surfaceTexture) {
                // todo 外部纹理可用，可以在这里开发预览
                cameraThread = new CameraThread(Camera2Activity.this);
                cameraThread.start();
                cameraThread.surfaceCreate(surfaceTexture);
            }
        };
    }

    private void initView() {
        glTextureView = new CameraGLTextureView(this);
        camera2Render = new Camera2Render();
        camera2Render.setCamera2RenderCallBack(camera2RenderCallBack);
        glTextureView.setRender(camera2Render);
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(glTextureView, vl);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
