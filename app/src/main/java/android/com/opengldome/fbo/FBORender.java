package android.com.opengldome.fbo;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.utils.CommonUtils;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FBORender implements GLSurfaceView.Renderer {

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

    private int mFBOProgramObject;
    private int vFBOPosition;
    private int vFBOTexcoord;
    private int uFBOTexture;

    private int mColorTexture;
    private int mFrameBuffers;

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

    private int mWidth;
    private int mHeight;

    public FBORender() {
        bPointBuffer = CommonUtils.fToB(sPoint);
        mbColorBuffer = CommonUtils.fToB(sColor);
        bPos = CommonUtils.fToB(sPos);
        bCoord = CommonUtils.fToB(sCoord);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgramObject = CommonUtils.createProgram(Application.getInstance(), R.raw.basis_frag, R.raw.basis_vert);
        vPosition = GLES30.glGetAttribLocation(mProgramObject, "vPosition");
        vColor = GLES30.glGetAttribLocation(mProgramObject, "vColor");

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

        mColorTexture = CommonUtils.newTexture(1, mWidth, mHeight);

        int[] frameBuffers = new int[1];
        GLES30.glGenFramebuffers(1, frameBuffers, 0);
        mFrameBuffers = frameBuffers[0];

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, mColorTexture, 0);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, 0);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_STENCIL_ATTACHMENT, GLES30.GL_RENDERBUFFER, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glViewport(0, 0, mWidth, mHeight);
        GLES30.glEnable(GLES30.GL_TEXTURE_2D);

        // 先画到帧缓冲区
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers);
        GLES30.glClearColor(1, 1, 1, 1);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glUseProgram(mProgramObject);
        GLES30.glVertexAttribPointer(vPosition, 3, GLES30.GL_FLOAT, false, 0, bPointBuffer);
        GLES30.glEnableVertexAttribArray(vPosition);
        GLES30.glVertexAttribPointer(vColor, 3, GLES30.GL_FLOAT, false, 0, mbColorBuffer);
        GLES30.glEnableVertexAttribArray(vColor);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        // 再画到屏幕上
        GLES30.glClearColor(1, 1, 1, 1);
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
