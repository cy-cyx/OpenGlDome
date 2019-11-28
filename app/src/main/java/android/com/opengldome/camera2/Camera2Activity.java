package android.com.opengldome.camera2;

import android.com.opengldome.egl.GLTextureView;
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

    private GLTextureView glTextureView;
    private Camera2Render camera2Render;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glTextureView = new GLTextureView(this);
        camera2Render = new Camera2Render();
        glTextureView.setRender(camera2Render);
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(glTextureView, vl);
    }
}
