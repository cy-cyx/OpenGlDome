package android.com.opengldome.shadows;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.utils.CommonUtils;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

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
            -.3f, .3f, -.3f, 0.f, 0.f, -1.f,
            .3f, .3f, -.3f, 0.f, 0.f, -1.f,
            -.3f, -.3f, -.3f, 0.f, 0.f, -1.f,

            .3f, .3f, -.3f, 0.f, 0.f, -1.f,
            -.3f, -.3f, -.3f, 0.f, 0.f, -1.f,
            .3f, -.3f, -.3f, 0.f, 0.f, -1.f,

            // 前面
            -.3f, .3f, .3f, 0.f, 0.f, 1.f,
            .3f, .3f, .3f, 0.f, 0.f, 1.f,
            -.3f, -.3f, .3f, 0.f, 0.f, 1.f,

            .3f, .3f, .3f, 0.f, 0.f, 1.f,
            -.3f, -.3f, .3f, 0.f, 0.f, 1.f,
            .3f, -.3f, .3f, 0.f, 0.f, 1.f,

            // 上面
            -.3f, .3f, .3f, 0.f, 1.f, 0.f,
            .3f, .3f, .3f, 0.f, 1.f, 0.f,
            -.3f, .3f, -.3f, 0.f, 1.f, 0.f,

            .3f, .3f, .3f, 0.f, 1.f, 0.f,
            -.3f, .3f, -.3f, 0.f, 1.f, 0.f,
            .3f, .3f, -.3f, 0.f, 1.f, 0.f,

            // 下面
            -.3f, -.3f, .3f, 0.f, -1.f, 0.f,
            .3f, -.3f, .3f, 0.f, -1.f, 0.f,
            -.3f, -.3f, -.3f, 0.f, -1.f, 0.f,

            .3f, -.3f, .3f, 0.f, -1.f, 0.f,
            -.3f, -.3f, -.3f, 0.f, -1.f, 0.f,
            .3f, -.3f, -.3f, 0.f, -1.f, 0.f,

            // 左面
            -.3f, -.3f, .3f, -1.f, 0.f, 0.f,
            -.3f, .3f, .3f, -1.f, 0.f, 0.f,
            -.3f, -.3f, -.3f, -1.f, 0.f, 0.f,

            -.3f, .3f, .3f, -1.f, 0.f, 0.f,
            -.3f, -.3f, -.3f, -1.f, 0.f, 0.f,
            -.3f, .3f, -.3f, -1.f, 0.f, 0.f,

            // 右面
            .3f, -.3f, .3f, 1.f, 0.f, 0.f,
            .3f, .3f, .3f, 1.f, 0.f, 0.f,
            .3f, -.3f, -.3f, 1.f, 0.f, 0.f,

            .3f, .3f, .3f, 1.f, 0.f, 0.f,
            .3f, -.3f, -.3f, 1.f, 0.f, 0.f,
            .3f, .3f, -.3f, 1.f, 0.f, 0.f,
    };
    private FloatBuffer bPosWithN;

    // 点光源的位置
    private final float[] sLight = new float[]{5.f, 6.f, -4.f, 1.f};

    private float[] mLightMatrix = new float[16];

    //  眼睛位置
    private final float[] sEye = new float[]{6.f, 6.f, 7.f, 1.f};

    private float[] mEyeMatrix = new float[16];

    private int mShadowProgramObject;
    private int vShadowPosition;
    private int uShadowTexture;
    private int uEyeMatrix;
    private int uLightMatrix;

    private FloatBuffer bPos;
    private final float[] sPos = {
            -1.f, -.3f, 1.f,
            1.f, -.3f, 1.f,
            -1.f, -.3f, -1.f,

            1.f, -.3f, 1.f,
            -1.f, -.3f, -1.f,
            1.f, -.3f, -1.f,
    };

    private int mLightProgramObject;

    private int vLightPosition;
    private int vLightNormal;
    private int uLightEyeMatrix;
    private int uObjectColor;
    private int uLightColor;
    private int uLightDir;
    private int uEyeLocal;

    // 物体颜色
    private float[] sObjectColor = new float[]{1.f, 0.f, 0.f, 1.f};
    private FloatBuffer bObjectColor;

    // 光颜色
    private float[] sLightColor = new float[]{1.f, 1.f, 1.f, 1.f};
    private FloatBuffer bLightColor;

    // 光的方向
    private FloatBuffer bLightDir;

    // 眼镜的位置
    private FloatBuffer bEyeLocal;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        bPosWithN = CommonUtils.fToB(sPosWithN);
        bPos = CommonUtils.fToB(sPos);

        bObjectColor = CommonUtils.fToB(sObjectColor);
        bLightColor = CommonUtils.fToB(sLightColor);
        bLightDir = CommonUtils.fToB(sLight);
        bEyeLocal = CommonUtils.fToB(sEye);

        // 仅仅为了获得深度纹理
        mProgramObjectDepth = CommonUtils.createProgram(Application.getInstance(), R.raw.depth_frag, R.raw.depth_vert);
        vDepPosition = GLES30.glGetAttribLocation(mProgramObjectDepth, "vPosition");
        uDepMatrix = GLES30.glGetUniformLocation(mProgramObjectDepth, "uMatrix");

        // 通过深度纹理，从光的角度判断存在平点面的深度是否比深度纹理内的深度高，如果比如深度
        // 不比深度纹理记录的高度高，说明被遮盖，处于阴影之中
        mShadowProgramObject = CommonUtils.createProgram(Application.getInstance(), R.raw.shadow_frag, R.raw.shadow_vert);
        vShadowPosition = GLES30.glGetAttribLocation(mShadowProgramObject, "vPosition");
        uShadowTexture = GLES30.glGetUniformLocation(mShadowProgramObject, "uDepthTexture");
        uEyeMatrix = GLES30.glGetUniformLocation(mShadowProgramObject, "uEyeMatrix");
        uLightMatrix = GLES30.glGetUniformLocation(mShadowProgramObject, "uLightMatrix");

        // 画光照的物体
        mLightProgramObject = CommonUtils.createProgram(Application.getInstance(), R.raw.light_frag, R.raw.light_vert);
        vLightPosition = GLES30.glGetAttribLocation(mLightProgramObject, "vPosition");
        vLightNormal = GLES30.glGetAttribLocation(mLightProgramObject, "vNormal");
        uLightEyeMatrix = GLES30.glGetUniformLocation(mLightProgramObject, "uMVPMatrix");
        uObjectColor = GLES30.glGetUniformLocation(mLightProgramObject, "uObjectColor");
        uLightColor = GLES30.glGetUniformLocation(mLightProgramObject, "uLightColor");
        uLightDir = GLES30.glGetUniformLocation(mLightProgramObject, "uLightDir");
        uEyeLocal = GLES30.glGetUniformLocation(mLightProgramObject, "uEyeLocal");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
        float ratio = (float) width / (float) height;

        // 光的mvp矩阵
        float[] tempMatrix = new float[16];
        float[] tempMatrix1 = new float[16];
        Matrix.frustumM(tempMatrix, 0, -1, 1, -1, 1, 4f, 20f);
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
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_FUNC, GLES30.GL_LEQUAL);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT, 1024, 1024, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_UNSIGNED_SHORT, null);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

        int[] frameBuffers = new int[1];
        GLES30.glGenFramebuffers(1, frameBuffers, 0);
        mFrameBuffers = frameBuffers[0];
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, mDepthTexture, 0);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_READ_FRAMEBUFFER, 0);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_STENCIL_ATTACHMENT, GLES30.GL_RENDERBUFFER, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException("缓冲区不完整");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glViewport(0, 0, 1024, 1024);

        GLES30.glEnable(GLES30.GL_TEXTURE_2D);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_LEQUAL);

        // 先画到帧缓冲区，获得深度纹理
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClearColor(1.f, 1.f, 1.f, 1.f);
        GLES30.glUseProgram(mProgramObjectDepth);
        bPosWithN.position(0);
        GLES30.glVertexAttribPointer(vDepPosition, 3, GLES30.GL_FLOAT, false, 8 * 3, bPosWithN);
        GLES30.glEnableVertexAttribArray(vDepPosition);
        GLES30.glUniformMatrix4fv(uDepMatrix, 1, false, mLightMatrix, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        GLES30.glViewport(0, 0, mWidth, mHeight);

        // 画阴影
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glUseProgram(mShadowProgramObject);
        GLES30.glVertexAttribPointer(vShadowPosition, 3, GLES30.GL_FLOAT, false, 0, bPos);
        GLES30.glEnableVertexAttribArray(vShadowPosition);
        GLES30.glUniformMatrix4fv(uLightMatrix, 1, false, mLightMatrix, 0);
        GLES30.glUniformMatrix4fv(uEyeMatrix, 1, false, mEyeMatrix, 0);
        GLES30.glUniform1i(uShadowTexture, 1);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        // 画带光的物体
        GLES30.glUseProgram(mLightProgramObject);
        bPosWithN.position(0);
        GLES30.glVertexAttribPointer(vLightPosition, 3, GLES30.GL_FLOAT, false, 8 * 3, bPosWithN);
        GLES30.glEnableVertexAttribArray(vLightPosition);
        bPosWithN.position(3);
        GLES30.glVertexAttribPointer(vLightNormal, 3, GLES30.GL_FLOAT, false, 8 * 3, bPosWithN);
        GLES30.glEnableVertexAttribArray(vLightNormal);
        GLES30.glUniformMatrix4fv(uLightEyeMatrix, 1, false, mEyeMatrix, 0);
        GLES30.glUniform4fv(uObjectColor, 1, bObjectColor);
        GLES30.glUniform4fv(uLightColor, 1, bLightColor);
        GLES30.glUniform4fv(uLightDir, 1, bLightDir);
        GLES30.glUniform4fv(uEyeLocal, 1, bEyeLocal);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36);
    }
}
