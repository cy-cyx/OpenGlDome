package android.com.opengldome.texture;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.utils.CommonUtils;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * create by cy
 * time : 2019/10/28
 * version : 1.0
 * Features :
 */
public class TextureRender implements GLSurfaceView.Renderer {

    private int mWidth;
    private int mHeight;

    private int mProgramObject;
    private int vPosition;
    private int vTexcoord;
    private int uTexture;

    private FloatBuffer bPos;
    private final float[] sPos = {
            -1.0f, 1.f, 0f,      // 左上角
            -1.0f, -1.0f, 0f,    // 左下角
            1.0f, 1f, 0f,        // 右上角
            1.0f, -1.f, 0f       // 右下角
    };

    private FloatBuffer bCoord;
    private final float[] sCoord = {
            0f, 0f,             // 左上角
            0f, 1f,              // 左下角
            1f, 0f,              // 右上角
            1f, 1f              // 右下角
    };

    private int mTexture;

    public TextureRender() {
        bPos = CommonUtils.fToB(sPos);
        bCoord = CommonUtils.fToB(sCoord);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgramObject = CommonUtils.createProgram(Application.getInstance(), R.raw.texture_frag, R.raw.texture_vert);

        vPosition = GLES30.glGetAttribLocation(mProgramObject, "vPosition");
        vTexcoord = GLES30.glGetAttribLocation(mProgramObject, "vTexcoord");

        GLES30.glVertexAttribPointer(vPosition, 3, GLES30.GL_FLOAT, false, 0, bPos);
        GLES30.glEnableVertexAttribArray(vPosition);

        GLES30.glVertexAttribPointer(vTexcoord, 2, GLES30.GL_FLOAT, false, 0, bCoord);
        GLES30.glEnableVertexAttribArray(vTexcoord);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        mTexture = CommonUtils.newTexture(mWidth, mHeight, BitmapFactory.decodeResource(Application.getInstance().getResources(), R.drawable.pic1));
        GLES30.glUniform1i(uTexture, mTexture);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glViewport(0, 0, mWidth, mHeight);

        GLES30.glEnable(GLES30.GL_TEXTURE_2D);//启用纹理贴图

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glClearColor(1.f, 1.f, 1.f, 1.f);

        GLES30.glUseProgram(mProgramObject);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
    }
}
