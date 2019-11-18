package android.com.opengldome.particsystem;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PSActivity extends AppCompatActivity {

    private GLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGlSurfaceView = new GLSurfaceView(this);
        mGlSurfaceView.setEGLContextClientVersion(3);
        mGlSurfaceView.setRenderer(new PSRender());
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
