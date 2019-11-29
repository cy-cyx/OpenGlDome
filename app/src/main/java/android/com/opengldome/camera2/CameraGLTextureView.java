package android.com.opengldome.camera2;

import android.com.opengldome.egl.GLTextureView;
import android.content.Context;

/**
 * create by cy
 * time : 2019/11/29
 * version : 1.0
 * Features :
 */
public class CameraGLTextureView extends GLTextureView {

    public CameraGLTextureView(Context context) {
        super(context);
    }

    @Override
    public void setRender(GlRender glRender) {
        ((Camera2Render) glRender).setGlTextureView(this);
        super.setRender(glRender);
    }
}
