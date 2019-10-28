package android.com.opengldome.mvp;

import android.com.opengldome.Application;
import android.com.opengldome.utils.CommonUtils;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MVPRender implements GLSurfaceView.Renderer {

    int mProgramObject;

    float[] mPoint = {-1f, -1f, 0f,
            0f, 1f, 0f,
            1f, -1f, 0f};
    FloatBuffer mPointBuffer;

    float[] mColor = {1f, 1f, 0f,
            1f, 0f, 1f,
            0f, 1f, 1f};
    FloatBuffer mColorBuffer;

    float[] mvpMatrix = new float[16];
    float[] mViewMatrax = new float[16];
    float[] mProjectionMatrix = new float[16];

    int mWidth = 1080;
    int mHeight = 1680;

    public MVPRender() {
        mPointBuffer = CommonUtils.fToB(mPoint);
        mColorBuffer = CommonUtils.fToB(mColor);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgramObject = CommonUtils.createProgram(Application.getInstance(),
                "common/fragmentMvp.glsl","common/vertexMvp.glsl");

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, mColorBuffer);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 0, mPointBuffer);
        GLES30.glEnableVertexAttribArray(1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        float ratio = (float) width / height;
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
        GLES30.glClearColor(1, 1, 1, 1);
        GLES30.glUseProgram(mProgramObject);
        GLES30.glUniformMatrix4fv(2, 1, false, mvpMatrix, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
    }
}
