package android.com.opengldome.light;

import android.com.opengldome.Application;
import android.com.opengldome.utils.CommonUtils;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

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

    float[] mvpMatrix = new float[16];
    float[] mViewMatrax = new float[16];
    float[] mProjectionMatrix = new float[16];

    /**
     * 点加法线
     */
    private final float[] sPosWithN = {
            // 前面
            -.5f, .5f, .5f, 0.f, 0.f, 1.f,
            .5f, .5f, .5f, 0.f, 0.f, 1.f,
            -.5f, -.5f, .5f, 0.f, 0.f, 1.f,

            .5f, .5f, .5f, 0.f, 0.f, 1.f,
            -.5f, -.5f, .5f, 0.f, 0.f, 1.f,
            .5f, -.5f, .5f, 0.f, 0.f, 1.f,

            // 后面
            -.5f, .5f, -.5f, 0.f, 0.f, -1.f,
            .5f, .5f, -.5f, 0.f, 0.f, -1.f,
            -.5f, -.5f, -.5f, 0.f, 0.f, -1.f,

            .5f, .5f, -.5f, 0.f, 0.f, -1.f,
            -.5f, -.5f, -.5f, 0.f, 0.f, -1.f,
            .5f, -.5f, -.5f, 0.f, 0.f, -1.f,

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

            // 右面
            .5f, -.5f, .5f, 1.f, 0.f, 0.f,
            .5f, .5f, .5f, 1.f, 0.f, 0.f,
            .5f, -.5f, -.5f, 1.f, 0.f, 0.f,

            .5f, .5f, .5f, 1.f, 0.f, 0.f,
            .5f, -.5f, -.5f, 1.f, 0.f, 0.f,
            .5f, .5f, -.5f, 1.f, 0.f, 0.f,

            // 左面
            -.5f, -.5f, .5f, -1.f, 0.f, 0.f,
            -.5f, .5f, .5f, -1.f, 0.f, 0.f,
            -.5f, -.5f, -.5f, -1.f, 0.f, 0.f,

            -.5f, .5f, .5f, -1.f, 0.f, 0.f,
            -.5f, -.5f, -.5f, -1.f, 0.f, 0.f,
            -.5f, .5f, -.5f, -1.f, 0.f, 0.f,
    };
    private FloatBuffer bPosWithN;

    // 物体颜色
    private float[] sObjectColor = new float[]{0.f, 0.f, 1.f, 1.f};
    private FloatBuffer bObjectColor;

    // 光颜色
    private float[] sLightColor = new float[]{1.f, 1.f, 1.f, 1.f};
    private FloatBuffer bLightColor;

    // 光的方向
    private float[] sLightDir = new float[]{-6.f, 10.f, 12.f, 1.f};
    private FloatBuffer bLightDir;

    // 眼睛的位置
    private float[] sEyeLocal = new float[]{-3.f, 4.f, -7.f, 1.f};
    private FloatBuffer bEyeLocal;

    public LightRender() {
        bPosWithN = CommonUtils.fToB(sPosWithN);
        bObjectColor = CommonUtils.fToB(sObjectColor);
        bLightColor = CommonUtils.fToB(sLightColor);
        bLightDir = CommonUtils.fToB(sLightDir);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFragment = CommonUtils.loadShader(Application.getInstance(), GLES30.GL_FRAGMENT_SHADER, "light/fragment.glsl");
        mVertex = CommonUtils.loadShader(Application.getInstance(), GLES30.GL_VERTEX_SHADER, "light/vertex.glsl");

        mProgramObject = GLES30.glCreateProgram();
        GLES30.glAttachShader(mProgramObject, mFragment);
        GLES30.glAttachShader(mProgramObject, mVertex);

        GLES30.glLinkProgram(mProgramObject);

        // 点
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 8 * 3, bPosWithN);
        GLES30.glEnableVertexAttribArray(0);

        // 法线
        bPosWithN.position(3);
        GLES30.glVertexAttribPointer(2, 3, GLES30.GL_FLOAT, false, 8 * 3, bPosWithN);
        GLES30.glEnableVertexAttribArray(2);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // 为了实现旋转
        upDataEye();

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glViewport(0, 0, mWidth, mHeight);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glClearColor(0, .5f, 0, 1);

        GLES30.glUseProgram(mProgramObject);

        initMvpMatrix();
        GLES30.glUniformMatrix4fv(1, 1, false, mvpMatrix, 0);

        // 物体颜色
        GLES30.glUniform4fv(3, 1, bObjectColor);
        // 环境光颜色
        GLES30.glUniform4fv(4, 1, bLightColor);
        // 光的方向（平行光）
        GLES30.glUniform4fv(5, 1, bLightDir);
        // 眼睛的位置
        GLES30.glUniform4fv(6, 1, CommonUtils.fToB(sEyeLocal));

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36);
    }

    private void initMvpMatrix() {
        float ratio = (float) mWidth / mHeight;
        //设置透视投影
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 4f, 20f);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrax, 0, sEyeLocal[0], sEyeLocal[1], sEyeLocal[2], 0f, 0f, 0f, 0f, 1f, 0f);
        //计算变换矩阵
        Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mViewMatrax, 0);
    }

    private int mAngle = 0;

    float i = 0; // 控速

    private void upDataEye() {
        i++;
        if (i < 3) return;
        i = 0;

        mAngle += 1;
        if (mAngle == 360) {
            mAngle = 0;
        }
        double b = Math.toRadians(mAngle);
        sEyeLocal[0] = (float) (7f * Math.sin(b));
        sEyeLocal[2] = (float) (7f * Math.cos(b));
    }
}
