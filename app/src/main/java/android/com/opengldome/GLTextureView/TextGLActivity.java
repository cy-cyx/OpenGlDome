package android.com.opengldome.GLTextureView;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * create by cy
 * time : 2019/11/19
 * version : 1.0
 * Features :
 */
public class TextGLActivity extends AppCompatActivity {

    private GLTextureView mGlTextureView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlTextureView = new GLTextureView(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlTextureView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlTextureView.onResume();
    }
}
