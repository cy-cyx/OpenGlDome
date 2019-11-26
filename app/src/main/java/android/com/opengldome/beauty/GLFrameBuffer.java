package android.com.opengldome.beauty;

import android.opengl.GLES30;

/**
 * create by cy
 * time : 2019/11/26
 * version : 1.0
 * Features : 包含多个frameBuffer，可相互切换。
 * 帧缓冲区的复用，在多层级滤镜绘制可用
 */
public class GLFrameBuffer {

    private int sumFrameBuffer = 0;
    private int curFrameBuffer = 0;

    private int[] textureArray;
    private int[] frameBufferArray;

    private int width = 1024;
    private int height = 1024;

    public GLFrameBuffer(int sum, int width, int height) {
        sumFrameBuffer = sum;
        this.width = width;
        this.height = height;
        textureArray = new int[sum];
        frameBufferArray = new int[sum];
        initFrameBuffer();
    }

    private void initFrameBuffer() {
        GLES30.glGenTextures(4, textureArray, 0);
        GLES30.glGenFramebuffers(4, frameBufferArray, 0);
        for (int index = 0; index < sumFrameBuffer; index++) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureArray[index]);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_GREATER);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_GREATER);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferArray[index]);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, textureArray[index], 0);
            GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, 0);
            GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_STENCIL_ATTACHMENT, GLES30.GL_RENDERBUFFER, 0);
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }

    public int getTexture() {
        return textureArray[curFrameBuffer];
    }

    public int getFrameBuffer() {
        return frameBufferArray[curFrameBuffer];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void nextFrameBuffer() {
        curFrameBuffer++;
        curFrameBuffer = curFrameBuffer % 4;
    }
}
