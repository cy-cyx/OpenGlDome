package android.com.opengldome.shadows;

import android.com.opengldome.Application;
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

    // 点光源的位置
    private final float[] sLight = new float[]{7.f, 7.f, 7.f};
    private FloatBuffer bLight;

    private float[] mLightMatrix;

    //  眼睛位置
    private final float[] sEye = new float[]{7.f, -7.f, 7.f};
    private FloatBuffer bEye;

    private float[] mEyeMatrix;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glEnable(GLES30.GL_DEPTH);
        bPosWithN = CommonUtils.fToB(sPosWithN);
        bLight = CommonUtils.fToB(sLight);

        //  深度纹理
        mProgramObjectDepth = CommonUtils.createProgram(Application.getInstance(),
                "fragmentMvp.glsl", "vertexMvp.glsl");


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
        Matrix.setLookAtM(tempMatrix1, 0, mLightMatrix[0], mLightMatrix[1], mLightMatrix[2], 0f, 0f, 0f, 0f, 1f, 0f);
        //计算变换矩阵
        Matrix.multiplyMM(mLightMatrix, 0, tempMatrix, 0, tempMatrix1, 0);

        // 眼睛mvp矩阵
        Matrix.frustumM(tempMatrix, 0, -ratio, ratio, -1, 1, 4f, 20f);
        //设置相机位置
        Matrix.setLookAtM(tempMatrix1, 0, mEyeMatrix[0], mEyeMatrix[1], mEyeMatrix[2], 0f, 0f, 0f, 0f, 1f, 0f);
        //计算变换矩阵
        Matrix.multiplyMM(mEyeMatrix, 0, tempMatrix, 0, tempMatrix1, 0);


    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
