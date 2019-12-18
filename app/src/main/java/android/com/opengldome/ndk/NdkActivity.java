package android.com.opengldome.ndk;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * create by cy
 * time : 2019/12/18
 * version : 1.0
 * Features : 使用C语言代码加载着色器
 */
public class NdkActivity extends AppCompatActivity {

    private GLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGlSurfaceView = new GLSurfaceView(this);
        mGlSurfaceView.setEGLContextClientVersion(3);
        mGlSurfaceView.setRenderer(new NdkRender());
        setContentView(mGlSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlSurfaceView.onResume();
    }
}
