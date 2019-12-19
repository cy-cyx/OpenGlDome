package android.com.opengldome.ndk;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.utils.CommonUtils;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * create by cy
 * time : 2019/12/18
 * version : 1.0
 * Features :
 */
public class NdkRender implements GLSurfaceView.Renderer {

    private int mWidth;
    private int mHeight;

    private int mProgramObject;
    private int vPosition;
    private int vColor;

    private float[] sPoint = {-1f, -1f, 0f,
            0f, 1f, 0f,
            1f, -1f, 0f};
    private FloatBuffer bPointBuffer;

    private float[] sColor = {1f, 1f, 0f,
            1f, 0f, 1f,
            0f, 1f, 1f};
    private FloatBuffer mbColorBuffer;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        bPointBuffer = CommonUtils.fToB(sPoint);
        mbColorBuffer = CommonUtils.fToB(sColor);

        mProgramObject = Gllib.creatBaseProgram();
        vPosition = GLES30.glGetAttribLocation(mProgramObject, "vPosition");
        vColor = GLES30.glGetAttribLocation(mProgramObject, "vColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glViewport(0, 0, mWidth, mHeight);

        GLES30.glClearColor(1, 1, 1, 1);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glUseProgram(mProgramObject);
        GLES30.glVertexAttribPointer(vPosition, 3, GLES30.GL_FLOAT, false, 0, bPointBuffer);
        GLES30.glEnableVertexAttribArray(vPosition);
        GLES30.glVertexAttribPointer(vColor, 3, GLES30.GL_FLOAT, false, 0, mbColorBuffer);
        GLES30.glEnableVertexAttribArray(vColor);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
    }
}
