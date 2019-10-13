package android.com.opengldome.blend;

import android.com.opengldome.R;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BlendActivity extends AppCompatActivity {

    private FrameLayout.LayoutParams mFl;
    private FrameLayout mContainerFl;
    private GLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContainerFl = new FrameLayout(this);
        mFl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(mContainerFl, mFl);
        {
            mGlSurfaceView = new GLSurfaceView(this);
            mGlSurfaceView.setEGLContextClientVersion(3);
            mGlSurfaceView.setRenderer(new BlendRender(this));
            mFl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mContainerFl.addView(mGlSurfaceView, mFl);
        }
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
