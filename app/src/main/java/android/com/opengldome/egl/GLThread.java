package android.com.opengldome.egl;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Size;

import java.lang.ref.WeakReference;

/**
 * create by cy
 * time : 2019/11/28
 * version : 1.0
 * Features : Gl线程
 */
public class GLThread extends Thread {

    private static final int MSG_SURFACE_CREATED = 1;  // surface创建
    private static final int MSG_SURFACE_CHANGED = 2;  // surface改变
    private static final int MSG_REQUEST_RENDER = 3;   // 请求绘制
    private static final int MSG_SURFACE_DESTROY = 4;  // surface销毁
    private static final int MSG_RELEASE = 5;           // 回收

    private EGLHelp eglHelp;
    private volatile GLHandler gLHandler;
    private GLTextureView.GlRender glRender;

    private boolean hasSurface = false;
    private int width = 0;
    private int height = 0;
    private boolean quit = false;

    public GLThread() {
        eglHelp = new EGLHelp();
    }

    public void setGlRender(GLTextureView.GlRender glRender) {
        this.glRender = glRender;
    }

    @Override
    public void run() {
        quit = false;
        Looper.prepare();
        eglHelp.start();
        glRender.onSurfaceCreate();
        synchronized (this) {
            gLHandler = new GLHandler(this);
            notify();
        }
        Looper.loop();
    }

    public void createSurface(SurfaceTexture surfaceTexture) {
        Message.obtain(getHandler(), MSG_SURFACE_CREATED, surfaceTexture).sendToTarget();
    }

    public void surfaceChanged(int width, int height) {
        Message.obtain(getHandler(), MSG_SURFACE_CHANGED, new Size(width, height)).sendToTarget();
    }

    public void requestRender() {
        Message.obtain(getHandler(), MSG_REQUEST_RENDER).sendToTarget();
    }

    public void surfaceDestroy() {
        Message.obtain(getHandler(), MSG_SURFACE_DESTROY).sendToTarget();
    }

    public void quit() {
        Message.obtain(getHandler(), MSG_RELEASE).sendToTarget();
    }

    private GLHandler getHandler() {
        synchronized (this) {
            while (gLHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return gLHandler;
        }
    }

    private void createSurfaceInner(SurfaceTexture surfaceTexture) {
        if (surfaceTexture != null) {
            hasSurface = eglHelp.createSurface(surfaceTexture);
        }
    }

    private void surfaceChangeInner(Size size) {
        width = size.getWidth();
        height = size.getHeight();
        glRender.onSurfaceChange(width, height);
    }

    private void swapInner() {
        if (canDraw()) {
            glRender.onDraw();
            eglHelp.swap();
        }
    }

    private void destroySurface() {
        hasSurface = false;
        eglHelp.destroySurface();
    }

    private void finishAndRelease() {
        eglHelp.destroySurface();
        eglHelp.finish();
        quitSafely();
    }

    private void quitSafely() {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            looper.quitSafely();
        }
    }

    private boolean canDraw() {
        return hasSurface && width > 0 && height > 0 && !quit;
    }

    private static class GLHandler extends Handler {

        private WeakReference<GLThread> glThreadWeakReference;

        GLHandler(GLThread glThread) {
            this.glThreadWeakReference = new WeakReference<>(glThread);
        }

        @Override
        public void handleMessage(Message msg) {
            GLThread glThread;
            switch (msg.what) {
                case MSG_SURFACE_CREATED:
                    glThread = glThreadWeakReference.get();
                    if (glThread != null) {
                        SurfaceTexture surfaceTexture = (SurfaceTexture) msg.obj;
                        glThread.createSurfaceInner(surfaceTexture);
                    }
                    break;
                case MSG_SURFACE_CHANGED:
                    glThread = glThreadWeakReference.get();
                    if (glThread != null) {
                        Size size = (Size) msg.obj;
                        glThread.surfaceChangeInner(size);
                    }
                    break;
                case MSG_REQUEST_RENDER:
                    glThread = glThreadWeakReference.get();
                    if (glThread != null) {
                        glThread.swapInner();
                    }
                    break;
                case MSG_SURFACE_DESTROY:
                    glThread = glThreadWeakReference.get();
                    if (glThread != null) {
                        glThread.destroySurface();
                    }
                    break;
                case MSG_RELEASE:
                    glThread = glThreadWeakReference.get();
                    if (glThread != null) {
                        glThread.finishAndRelease();
                    }
                    break;
            }
        }
    }
}
