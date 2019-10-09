package android.com.opengldome.blend;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BlendRender implements GLSurfaceView.Renderer {

    private int mWidth;
    private int mHeight;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        gl.glViewport(0, 0, mWidth, mHeight);
        gl.glClearColor(1f, 1f, 1f, 1f);
    }
}
