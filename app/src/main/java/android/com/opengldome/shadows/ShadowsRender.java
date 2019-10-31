package android.com.opengldome.shadows;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.utils.CommonUtils;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ShadowsRender implements GLSurfaceView.Renderer {


    private int mWidth;
    private int mHeight;

    private int mProgramObjectDepth;
    private int vDepPosition;
    private int uDepMatrix;

    private int mDepthTexture;
    private int mFrameBuffers;

    /**
     * 点加法线
     */
    private final float[] sPosWithN = {
            // 后面
            -.5f, .5f, -.5f, 0.f, 0.f, -1.f,
            .5f, .5f, -.5f, 0.f, 0.f, -1.f,
            -.5f, -.5f, -.5f, 0.f, 0.f, -1.f,

            .5f, .5f, -.5f, 0.f, 0.f, -1.f,
            -.5f, -.5f, -.5f, 0.f, 0.f, -1.f,
            .5f, -.5f, -.5f, 0.f, 0.f, -1.f,

            // 前面
            -.5f, .5f, .5f, 0.f, 0.f, 1.f,
            .5f, .5f, .5f, 0.f, 0.f, 1.f,
            -.5f, -.5f, .5f, 0.f, 0.f, 1.f,

            .5f, .5f, .5f, 0.f, 0.f, 1.f,
            -.5f, -.5f, .5f, 0.f, 0.f, 1.f,
            .5f, -.5f, .5f, 0.f, 0.f, 1.f,

            // 上面
            -.5f, .5f, .5f, 0.f, 1.f, 0.f,
            .5f, .5f, .5f, 0.f, 1.f, 0.f,
            -.5f, .5f, -.5f, 0.f, 1.f, 0.f,

            .5f, .5f, .5f, 0.f, 1.f, 0.f,
            -.5f, .5f, -.5f, 0.f, 1.f, 0.f,
            .5f, .5f, -.5f, 0.f, 1.f, 0.f,

            // 下面
            -.5f, -.5f, .5f, 0.f, -1.f, 0.f,
            .5f, -.5f, .5f, 0.f, -1.f, 0.f,
            -.5f, -.5f, -.5f, 0.f, -1.f, 0.f,

            .5f, -.5f, .5f, 0.f, -1.f, 0.f,
            -.5f, -.5f, -.5f, 0.f, -1.f, 0.f,
            .5f, -.5f, -.5f, 0.f, -1.f, 0.f,

            // 左面
            -.5f, -.5f, .5f, -1.f, 0.f, 0.f,
            -.5f, .5f, .5f, -1.f, 0.f, 0.f,
            -.5f, -.5f, -.5f, -1.f, 0.f, 0.f,

            -.5f, .5f, .5f, -1.f, 0.f, 0.f,
            -.5f, -.5f, -.5f, -1.f, 0.f, 0.f,
            -.5f, .5f, -.5f, -1.f, 0.f, 0.f,

            // 右面
            .5f, -.5f, .5f, 1.f, 0.f, 0.f,
            .5f, .5f, .5f, 1.f, 0.f, 0.f,
            .5f, -.5f, -.5f, 1.f, 0.f, 0.f,

            .5f, .5f, .5f, 1.f, 0.f, 0.f,
            .5f, -.5f, -.5f, 1.f, 0.f, 0.f,
            .5f, .5f, -.5f, 1.f, 0.f, 0.f,
    };
    private FloatBuffer bPosWithN;

    // 点光源的位置
    private final float[] sLight = new float[]{0.f, 0.f, -7.f};

    private float[] mLightMatrix = new float[16];

    //  眼睛位置
    private final float[] sEye = new float[]{7.f, -7.f, 7.f};

    private float[] mEyeMatrix = new float[16];

    private int mFBOProgramObject;
    private int vFBOPosition;
    private int vFBOTexcoord;
    private int uFBOTexture;

    private FloatBuffer bPos;
    private final float[] sPos = {
            -1.0f, 1.0f, 0f,      // 左上角
            -1.0f, -1.0f, 0f,    // 左下角
            1.0f, 1.0f, 0f,        // 右上角
            1.0f, -1.0f, 0f       // 右下角
    };

    // todo 由于安卓纹理坐标是左上角是原点、FBO纹理坐标的左下角是原点
    private FloatBuffer bCoord;
    private final float[] sCoord = {
            0f, 1f,             // 左上角
            0f, 0f,              // 左下角
            1f, 1f,              // 右上角
            1f, 0f              // 右下角
    };

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        bPosWithN = CommonUtils.fToB(sPosWithN);
        bPos = CommonUtils.fToB(sPos);
        bCoord = CommonUtils.fToB(sCoord);

        mProgramObjectDepth = CommonUtils.createProgram(Application.getInstance(), R.raw.depth_frag, R.raw.depth_vert);
        vDepPosition = GLES30.glGetAttribLocation(mProgramObjectDepth, "vPosition");
        uDepMatrix = GLES30.glGetUniformLocation(mProgramObjectDepth, "uMatrix");

        // 其实就是纹理贴图
        mFBOProgramObject = CommonUtils.createProgram(Application.getInstance(), R.raw.texture_frag, R.raw.texture_vert);
        vFBOPosition = GLES30.glGetAttribLocation(mFBOProgramObject, "vPosition");
        vFBOTexcoord = GLES30.glGetAttribLocation(mFBOProgramObject, "vTexcoord");
        uFBOTexture = GLES30.glGetUniformLocation(mFBOProgramObject, "uTexture");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
        float ratio = (float) width / (float) height;

        // 光的mvp矩阵
        float[] tempMatrix = new float[16];
        float[] tempMatrix1 = new float[16];
        Matrix.frustumM(tempMatrix, 0, -ratio, ratio, -1, 1, 4f, 20f);
        //设置相机位置
        Matrix.setLookAtM(tempMatrix1, 0, sLight[0], sLight[1], sLight[2], 0f, 0f, 0f, 0f, 1f, 0f);
        //计算变换矩阵
        Matrix.multiplyMM(mLightMatrix, 0, tempMatrix, 0, tempMatrix1, 0);

        // 眼睛mvp矩阵
        Matrix.frustumM(tempMatrix, 0, -ratio, ratio, -1, 1, 4f, 20f);
        //设置相机位置
        Matrix.setLookAtM(tempMatrix1, 0, sEye[0], sEye[1], sEye[2], 0f, 0f, 0f, 0f, 1f, 0f);
        //计算变换矩阵
        Matrix.multiplyMM(mEyeMatrix, 0, tempMatrix, 0, tempMatrix1, 0);

        // 获得深度纹理
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        mDepthTexture = textures[0];
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mDepthTexture);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_FUNC, GLES30.GL_LEQUAL );
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT, width, height, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_UNSIGNED_SHORT, null);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

        if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException("缓冲区不完整");
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

        int[] frameBuffers = new int[1];
        GLES30.glGenFramebuffers(1, frameBuffers, 0);
        mFrameBuffers = frameBuffers[0];
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, mDepthTexture, 0);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_READ_FRAMEBUFFER, 0);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_STENCIL_ATTACHMENT, GLES30.GL_RENDERBUFFER, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glViewport(0, 0, mWidth, mHeight);

        GLES30.glEnable(GLES30.GL_TEXTURE_2D);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_ALWAYS);

        // 先画到帧缓冲区，获得深度纹理
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glClearColor(1.f, 1.f, 1.f, 1.f);
        GLES30.glUseProgram(mProgramObjectDepth);
        GLES30.glVertexAttribPointer(vDepPosition, 3, GLES30.GL_FLOAT, false, 8 * 3, bPosWithN);
        GLES30.glEnableVertexAttribArray(vDepPosition);
        GLES30.glUniformMatrix4fv(uDepMatrix, 1, false, mLightMatrix, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        // 再画到屏幕上
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glUseProgram(mFBOProgramObject);
        GLES30.glVertexAttribPointer(vFBOPosition, 3, GLES30.GL_FLOAT, false, 0, bPos);
        GLES30.glEnableVertexAttribArray(vFBOPosition);
        GLES30.glVertexAttribPointer(vFBOTexcoord, 2, GLES30.GL_FLOAT, false, 0, bCoord);
        GLES30.glEnableVertexAttribArray(vFBOTexcoord);
        GLES30.glUniform1i(uFBOTexture, 1);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
    }
}
