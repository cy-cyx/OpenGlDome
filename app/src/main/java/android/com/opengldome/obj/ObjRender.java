package android.com.opengldome.obj;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.utils.CommonUtils;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ObjRender implements GLSurfaceView.Renderer {

    private int mWidth;
    private int mHeight;

    private int mProgramObject;
    private int vPosition;
    private int uMatrix;
    private int vCoord;
    private int uTexture;
    private int vNormal;
    private int uLightColor;
    private int uLightDir;
    private int uEyeLocal;

    private float[] fData; // 点+纹理+法线
    private FloatBuffer bData;

    // 眼睛的位置
    private float[] sEyeLocal = new float[]{-0.f, -6.f, 1.f, 1.f};

    // 光颜色
    private float[] sLightColor = new float[]{1.f, 1.f, 1.f, 1.f};
    private FloatBuffer bLightColor;

    // 光的方向
    private float[] sLightDir = new float[]{-0.f, -6.f, 1.f, 1.f};
    private FloatBuffer bLightDir;

    private float[] mvpMatrix = new float[16];

    private int mTexture;

    public void setData(float[] data) {
        this.fData = data;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        bData = CommonUtils.fToB(fData);
        bLightColor = CommonUtils.fToB(sLightColor);
        bLightDir = CommonUtils.fToB(sLightDir);

        mProgramObject = CommonUtils.createProgram(Application.getInstance(), R.raw.obj_frag, R.raw.obj_vert);
        vPosition = GLES30.glGetAttribLocation(mProgramObject, "vPosition");
        vCoord = GLES30.glGetAttribLocation(mProgramObject, "vCoord");
        vNormal = GLES30.glGetAttribLocation(mProgramObject, "vNormal");
        uMatrix = GLES30.glGetUniformLocation(mProgramObject, "uMatrix");
        uTexture = GLES30.glGetUniformLocation(mProgramObject, "uTexture");
        uLightColor = GLES30.glGetUniformLocation(mProgramObject, "uLightColor");
        uLightDir = GLES30.glGetUniformLocation(mProgramObject, "uLightDir");
        uEyeLocal = GLES30.glGetUniformLocation(mProgramObject, "uEyeLocal");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        mTexture = CommonUtils.newTexture(1, BitmapFactory.decodeResource(Application.getInstance().getResources(), R.drawable.dog_diffuse));
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glViewport(0, 0, mWidth, mHeight);

        GLES30.glEnable(GLES30.GL_TEXTURE_2D);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glClearColor(1.f, 1.f, 1.f, 1.f);

        GLES30.glUseProgram(mProgramObject);
        bData.position(0);
        // 偏移 一个float 为 4个字节 偏移5个
        GLES30.glVertexAttribPointer(vPosition, 3, GLES30.GL_FLOAT, false, 4 * 8, bData);
        GLES30.glEnableVertexAttribArray(vPosition);

        bData.position(3);
        // 偏移 一个float 为 4个字节 偏移5个
        GLES30.glVertexAttribPointer(vCoord, 2, GLES30.GL_FLOAT, false, 4 * 8, bData);
        GLES30.glEnableVertexAttribArray(vCoord);

        bData.position(5);
        // 偏移 一个float 为 4个字节 偏移5个
        GLES30.glVertexAttribPointer(vNormal, 2, GLES30.GL_FLOAT, false, 4 * 8, bData);
        GLES30.glEnableVertexAttribArray(vNormal);

        GLES30.glUniform1i(uTexture, 1);

        upDataEye();
        initMvpMatrix();
        GLES30.glUniformMatrix4fv(uMatrix, 1, false, mvpMatrix, 0);

        // 环境光颜色
        GLES30.glUniform4fv(uLightColor, 1, bLightColor);
        // 光的方向（平行光）
        GLES30.glUniform4fv(uLightDir, 1, bLightDir);
        // 眼睛的位置
        GLES30.glUniform4fv(uEyeLocal, 1, CommonUtils.fToB(sEyeLocal));

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, fData.length / 8 * 3);
    }

    private void initMvpMatrix() {
        float ratio = (float) mWidth / mHeight;

        float[] mProjectionMatrix = new float[16];
        float[] mViewMatrax = new float[16];

        //设置透视投影
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 4f, 20f);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrax, 0, sEyeLocal[0], sEyeLocal[1], sEyeLocal[2], 0f, 0f, 0f, 0f, 0f, 1f);
        //计算变换矩阵
        Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mViewMatrax, 0);
    }

    private int mAngle = 180;

    private float i = 0; // 控速

    private void upDataEye() {
        i++;
        if (i < 3) return;
        i = 0;

        mAngle += 1;
        if (mAngle == 360) {
            mAngle = 0;
        }
        double b = Math.toRadians(mAngle);
        sEyeLocal[0] = (float) (6f * Math.sin(b));
        sEyeLocal[1] = (float) (6f * Math.cos(b));
    }

}
