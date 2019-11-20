package android.com.opengldome.gltextureview;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.utils.CommonUtils;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextGLRender implements GLSurfaceView.Renderer {

    private int mProgramObject;

    private int vPosition;
    private int vColor;
    private int uMatrix;

    private float[] mPoint = {-1f, -1f, 0f,
            0f, 1f, 0f,
            1f, -1f, 0f};
    private FloatBuffer mPointBuffer;

    private float[] mColor = {1f, 1f, 0f,
            1f, 0f, 1f,
            0f, 1f, 1f};
    private FloatBuffer mColorBuffer;

    private float[] mvpMatrix = new float[16];

    private int mWidth;
    private int mHeight;

    public TextGLRender() {
        mPointBuffer = CommonUtils.fToB(mPoint);
        mColorBuffer = CommonUtils.fToB(mColor);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgramObject = CommonUtils.createProgram(Application.getInstance(),
                R.raw.mvp_frag, R.raw.mvp_vert);

        vPosition = GLES30.glGetAttribLocation(mProgramObject, "vPosition");
        vColor = GLES30.glGetAttribLocation(mProgramObject, "vColor");
        uMatrix = GLES30.glGetUniformLocation(mProgramObject, "uMatrix");

        GLES30.glVertexAttribPointer(vPosition, 3, GLES30.GL_FLOAT, false, 0, mPointBuffer);
        GLES30.glEnableVertexAttribArray(vPosition);
        GLES30.glVertexAttribPointer(vColor, 3, GLES30.GL_FLOAT, false, 0, mColorBuffer);
        GLES30.glEnableVertexAttribArray(vColor);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        float ratio = (float) width / height;
        float[] mProjectionMatrix = new float[16];
        float[] mViewMatrax = new float[16];

        //设置透视投影
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 4f, 7f);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrax, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mViewMatrax, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glViewport(0, 0, mWidth, mHeight);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glClearColor(1.f, 1.f, 1.f, 1.f);
        GLES30.glUseProgram(mProgramObject);
        GLES30.glUniformMatrix4fv(uMatrix, 1, false, mvpMatrix, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
    }
}
