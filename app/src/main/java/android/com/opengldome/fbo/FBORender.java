package android.com.opengldome.fbo;

import android.com.opengldome.Application;
import android.com.opengldome.utils.CommonUtils;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FBORender implements GLSurfaceView.Renderer {

    int mFBOFragment;
    int mFBOVertex;
    int mFBOProgramObject;

    int mFragment;
    int mVertex;
    int mProgramObject;

    float[] mFBOPoint = {-1f, -1f, 0f,
            0f, 1f, 0f,
            1f, -1f, 0f};
    FloatBuffer mFBOPointBuffer;

    float[] mFBOColor = {1f, 1f, 0f,
            1f, 0f, 1f,
            0f, 1f, 1f};
    FloatBuffer mFBOColorBuffer;

    int mColorTexture;
    int mFrameBuffers;

    private FloatBuffer bPos;
    private final float[] sPos = {
            -1.0f, 1.0f, 0f,      // 左上角
            -1.0f, -1.0f, 0f,    // 左下角
            1.0f, 1.0f, 0f,        // 右上角
            1.0f, -1.0f, 0f       // 右下角
    };

    private FloatBuffer bCoord;
    private final float[] sCoord = {
            0f, 0f,             // 左上角
            0f, 1f,              // 左下角
            1f, 0f,              // 右上角
            1f, 1f              // 右下角
    };

    int mTexture;

    int mWidth = 1080;
    int mHeight = 1680;

    public FBORender() {
        mFBOPointBuffer = CommonUtils.fToB(mFBOPoint);
        mFBOColorBuffer = CommonUtils.fToB(mFBOColor);
        bPos = CommonUtils.fToB(sPos);
        bCoord = CommonUtils.fToB(sCoord);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFBOFragment = CommonUtils.loadShader(Application.getInstance(), GLES30.GL_FRAGMENT_SHADER, "fragment.glsl");
        mFBOVertex = CommonUtils.loadShader(Application.getInstance(), GLES30.GL_VERTEX_SHADER, "vertex.glsl");

        mFBOProgramObject = GLES30.glCreateProgram();
        GLES30.glAttachShader(mFBOProgramObject, mFBOFragment);
        GLES30.glAttachShader(mFBOProgramObject, mFBOVertex);

        GLES30.glLinkProgram(mFBOProgramObject);

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, mFBOPointBuffer);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 0, mFBOColorBuffer);
        GLES30.glEnableVertexAttribArray(1);

        int[] texture = new int[1];
        GLES30.glGenTextures(1, texture, 0);

        mColorTexture = texture[0];
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mColorTexture);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, mWidth, mHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_GREATER);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_GREATER);

        int[] frameBuffers = new int[1];
        GLES30.glGenFramebuffers(1, frameBuffers, 0);
        mFrameBuffers = frameBuffers[0];

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, mColorTexture, 0);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, 0);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_STENCIL_ATTACHMENT, GLES30.GL_RENDERBUFFER, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        GLES30.glEnable(GLES30.GL_TEXTURE_2D);

        mFragment = CommonUtils.loadShader(Application.getInstance(), GLES30.GL_FRAGMENT_SHADER, "fbo/fragment.glsl");
        mVertex = CommonUtils.loadShader(Application.getInstance(), GLES30.GL_VERTEX_SHADER, "fbo/vertex.glsl");

        mProgramObject = GLES30.glCreateProgram();
        GLES30.glAttachShader(mFBOProgramObject, mFragment);
        GLES30.glAttachShader(mFBOProgramObject, mVertex);

        GLES30.glLinkProgram(mProgramObject);

        GLES30.glVertexAttribPointer(2, 3, GLES30.GL_FLOAT, false, 0, bPos);
        GLES30.glEnableVertexAttribArray(2);
        GLES30.glVertexAttribPointer(3, 2, GLES30.GL_FLOAT, false, 0, bCoord);
        GLES30.glEnableVertexAttribArray(3);

        mTexture = GLES30.glGetUniformLocation(mProgramObject, "vTexture");
        GLES30.glUniform1i(mTexture, mColorTexture);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        mWidth = width;
//        mHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glViewport(0, 0, mWidth, mHeight);

        // 先画到帧缓冲区
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClearColor(1, 1, 1, 0);
        GLES30.glUseProgram(mFBOProgramObject);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

        // 再画到屏幕上
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClearColor(1, 1, 1, 0);
        GLES30.glUseProgram(mProgramObject);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 4);
    }
}
