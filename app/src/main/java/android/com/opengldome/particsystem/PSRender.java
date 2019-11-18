package android.com.opengldome.particsystem;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.utils.CommonUtils;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 粒子系统 将一堆粒子起点、终点、持续时间 当作顶点属性输入 再传入当前时间作为变量 在片元着色器计算
 */
public class PSRender implements GLSurfaceView.Renderer {

    private int mWidth;
    private int mHeight;

    private int mProgramObject;
    private int vStartPoint;
    private int vEndPoint;
    private int vContinuedTime;
    private int uCurTime;

    private int mPointSum = 500;
    private int mTime = 600;   // 设置持续时间  GlSurfaceView 大概刷新时间1S 60次 最长10s
    private int mCurTime = Integer.MAX_VALUE;

    private float[] mPointStart = new float[mPointSum * 3];
    private FloatBuffer bPointStart;

    private float[] mPointEnd = new float[mPointSum * 3];
    private FloatBuffer bPointEnd;

    private float[] mPointTime = new float[mPointSum];
    private FloatBuffer bPointTime;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgramObject = CommonUtils.createProgram(Application.getInstance(), R.raw.partics_frag, R.raw.partics_vert);
        vStartPoint = GLES30.glGetAttribLocation(mProgramObject, "vStartPoint");
        vEndPoint = GLES30.glGetAttribLocation(mProgramObject, "vEndPoint");
        vContinuedTime = GLES30.glGetAttribLocation(mProgramObject, "vContinuedTime");
        uCurTime = GLES30.glGetUniformLocation(mProgramObject, "uCurTime");
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
        GLES30.glClearColor(1.f, 1.f, 1.f, 1.f);

        // 更新数据
        upData();
        GLES30.glUseProgram(mProgramObject);

        GLES30.glVertexAttribPointer(vStartPoint, 3, GLES30.GL_FLOAT, false, 0, bPointStart);
        GLES30.glEnableVertexAttribArray(vStartPoint);

        GLES30.glVertexAttribPointer(vEndPoint, 3, GLES30.GL_FLOAT, false, 0, bPointEnd);
        GLES30.glEnableVertexAttribArray(vEndPoint);


        GLES30.glVertexAttribPointer(vContinuedTime, 1, GLES30.GL_FLOAT, false, 0, bPointTime);
        GLES30.glEnableVertexAttribArray(vContinuedTime);

        GLES30.glUniform1f(uCurTime, mCurTime);

        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, mPointSum);
    }

    public void upData() {

        if (mCurTime > mTime) {
            mCurTime = 0;
            // 设置点开始区域（x -0.05~0.05 y -0.5~-0.4 z -0.05~0.05）
            for (int i = 0; i < mPointSum; i++) {
                mPointStart[3 * i] = (float) (-0.05f + new Random().nextInt(1000) / 1000f * .1f);
                mPointStart[1 + 3 * i] = (float) (-0.5f + new Random().nextInt(1000) / 1000f * .1f);
                mPointStart[2 + 3 * i] = (float) (-0.05f + new Random().nextInt(1000) / 1000f * .1f);
            }
            bPointStart = CommonUtils.fToB(mPointStart);

            // 设置点结束区域(x -0.3~0.3 y 0.6~0.9 z -0.3~0.3)
            for (int i = 0; i < mPointSum; i++) {
                mPointEnd[3 * i] = (float) (-0.3f + new Random().nextInt(1000) / 1000f * 0.6f);
                mPointEnd[1 + 3 * i] = (float) (0.6f + new Random().nextInt(1000) / 1000f * 0.3f);
                mPointEnd[2 + 3 * i] = (float) (-0.3f + new Random().nextInt(1000) / 1000f * 0.6f);
            }
            bPointEnd = CommonUtils.fToB(mPointEnd);

            for (int i = 0; i < mPointSum; i++) {
                mPointTime[i] = new Random().nextInt(mTime);
            }
            bPointTime = CommonUtils.fToB(mPointTime);
        }
        mCurTime++;
    }
}
