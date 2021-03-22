package android.com.opengldome.beauty;

import android.com.opengldome.utils.CommonUtils;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import java.nio.FloatBuffer;

/**
 * create by cy
 * time : 2019/11/26
 * version : 1.0
 * Features :
 */
public class AFilter {

    private final float[] sPos = {
            -1.0f, 1.0f, 0f,      // 左上角
            -1.0f, -1.0f, 0f,    // 左下角
            1.0f, 1.0f, 0f,        // 右上角
            1.0f, -1.0f, 0f       // 右下角
    };
    private FloatBuffer bPos;

    private final float[] sCoord = {
            0f, 0f,             // 左上角
            0f, 1f,              // 左下角
            1f, 0f,              // 右上角
            1f, 1f              // 右下角
    };
    protected FloatBuffer bCoord;

    private final float[] sOESCoord = {
            0f, 1f,             // 左上角
            0f, 0f,              // 左下角
            1f, 1f,              // 右上角
            1f, 0f              // 右下角
    };
    private FloatBuffer bOESCoord;

    // 导入资源纹理使用OES
    private boolean useOes = false;

    protected int program;

    protected int position;
    protected int texcoord;
    private int sourceTexture;

    private GLFrameBuffer glFrameBuffer;

    public AFilter(Context context, int vert, int frag) {
        bPos = CommonUtils.fToB(sPos);
        bCoord = CommonUtils.fToB(sCoord);
        bOESCoord = CommonUtils.fToB(sOESCoord);
        program = CommonUtils.createProgram(context, frag, vert);
        linkLocation();
    }

    protected void linkLocation() {
        position = GLES30.glGetAttribLocation(program, "vPosition");
        texcoord = GLES30.glGetAttribLocation(program, "vTexcoord");
        sourceTexture = GLES30.glGetUniformLocation(program, "uSourceImage");
    }

    public void onDraw(int src, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        useProgram();
        useFrameBuffer();
        clear();
        bindTexture(src);
        bindValue();
        drawArray();
        unBindValue();
        unBindTexture();
        clearProgram();
    }

    private void useFrameBuffer() {
        if (glFrameBuffer != null) {
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, glFrameBuffer.getFrameBuffer());
        } else {
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        }
    }

    protected void clear() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
    }

    protected void useProgram() {
        GLES30.glEnable(GLES30.GL_TEXTURE_2D);
        GLES30.glUseProgram(program);
    }

    /**
     * 活动单元1，固定给资源纹理
     */
    private void bindTexture(int src) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, src);
        GLES30.glUniform1i(sourceTexture, 1);
    }

    protected void bindValue() {
        GLES30.glVertexAttribPointer(position, 3, GLES30.GL_FLOAT, false, 0, bPos);
        GLES30.glEnableVertexAttribArray(position);
        GLES30.glVertexAttribPointer(texcoord, 2, GLES30.GL_FLOAT, false, 0, useOes ? bOESCoord : bCoord);
        GLES30.glEnableVertexAttribArray(texcoord);
    }

    protected void drawArray() {
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
    }

    protected void unBindValue() {
        GLES30.glDisableVertexAttribArray(position);
        GLES30.glDisableVertexAttribArray(texcoord);
    }

    private void unBindTexture() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES20.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    protected void clearProgram() {
        GLES30.glUseProgram(0);
        GLES30.glDisable(GLES30.GL_TEXTURE_2D);
    }

    public void setGlFrameBuffer(GLFrameBuffer glFrameBuffer) {
        this.glFrameBuffer = glFrameBuffer;
    }

    protected void onSurfaceChanged(int width, int height) {

    }

    public void setUseOes(boolean use) {
        this.useOes = use;
    }
}
