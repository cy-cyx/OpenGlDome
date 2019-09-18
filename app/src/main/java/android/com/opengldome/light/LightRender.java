package android.com.opengldome.light;

import android.com.opengldome.Application;
import android.com.opengldome.utils.CommonUtils;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * create by cy
 * time : 2019/9/18
 * version : 1.0
 * Features :
 */
public class LightRender implements GLSurfaceView.Renderer {

    private int mWidth;
    private int mHeight;

    int mFragment;
    int mVertex;
    int mProgramObject;

    /**
     * 点加法线
     */
    private final float[] sPosWithN = {
            -.5f, .5f, .5f, 0.f, 0.f, 1.f
    };

    public LightRender() {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFragment = CommonUtils.loadShader(Application.getInstance(), GLES30.GL_FRAGMENT_SHADER, "light/fragment.glsl");
        mVertex = CommonUtils.loadShader(Application.getInstance(), GLES30.GL_VERTEX_SHADER, "light/vertex.glsl");

        mProgramObject = GLES30.glCreateProgram();
        GLES30.glAttachShader(mProgramObject, mFragment);
        GLES30.glAttachShader(mProgramObject, mVertex);

        GLES30.glLinkProgram(mProgramObject);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glViewport(0, 0, mWidth, mHeight);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glClearColor(0, 0, 0, 1);
    }
}
