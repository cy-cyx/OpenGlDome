package android.com.opengldome.gltextureview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.TextureView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGL10;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;

/**
 * create by cy
 * time : 2019/11/19
 * version : 1.0
 * Features : {@link android.opengl.GLSurfaceView}
 */
public class GLTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    public GLTextureView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mGlThread.surfaceCreated(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mGlThread.onWindowResize(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mGlThread.surfaceDestroyed();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mDetached && mRenderer != null) {
            mGlThread = new GlThread(mThisWeakRef);
            mGlThread.start();
            mDetached = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mGlThread.requestExitAndWait();
        mDetached = true;
        super.onDetachedFromWindow();
    }

    @Override
    protected void finalize() throws Throwable {

        // 在这里确认杀Gl线程
        if (mGlThread != null)
            mGlThread.requestExitAndWait();

        super.finalize();
    }

    public void setRenderer(GLSurfaceView.Renderer render) {
        mRenderer = render;
        mGlThread = new GlThread(mThisWeakRef);
        mGlThread.start();
    }


    public void onPause() {
        mGlThread.onPause();
    }

    public void onResume() {
        mGlThread.onResume();
    }

    /**
     * Gl线程
     */
    static class GlThread extends Thread {

        GlThread(WeakReference<GLTextureView> glTextureViewWeakReference) {
            mGLSurfaceViewWeakRef = glTextureViewWeakReference;
        }

        @Override
        public void run() {
            try {
                guardedRun();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sGlThreadManager.threadExiting(this); // 保证外面任何请求在等锁，不会因为异常而变成死锁
            }
        }

        private void guardedRun() throws InterruptedException {

            mEglHelper = new EglHelper(mGLSurfaceViewWeakRef);
            mHaveEglContext = false;
            mHaveEglSurface = false;

            boolean createEglContext = false;
            boolean createEglSurface = false;
            boolean sizeChanged = false;
            int w = 0;
            int h = 0;

            try {
                // 外循环负责绘画

                while (true) {
                    synchronized (sGlThreadManager) {
                        // 内循环负责判断(需要同步)
                        while (true) {
                            // 退出
                            if (mShouldExit) {
                                return;
                            }

                            // 更新生命周期
                            boolean pausing = false;
                            if (mPaused != mRequestPaused) {
                                pausing = mRequestPaused;
                                mPaused = mRequestPaused;
                                sGlThreadManager.notifyAll();
                            }

                            // 更新surface

                            // lost surface
                            if (!mHasSurface && !mWaitingForSurface) {
                                if (mHaveEglSurface) {
                                    stopEglSurfaceLocked();
                                }
                                mWaitingForSurface = true;
                                sGlThreadManager.notifyAll();
                            }

                            // acquired surface
                            if (mHasSurface && mWaitingForSurface) {
                                mWaitingForSurface = false;
                                sGlThreadManager.notifyAll();
                            }


                            if (readyToDraw()) {

                                // 先确认有没有EGLContext
                                if (!mHaveEglContext) {
                                    try {
                                        mEglHelper.start();
                                    } catch (RuntimeException t) {
                                        throw t;
                                    }
                                    mHaveEglContext = true;
                                    createEglContext = true;
                                }

                                if (!mHaveEglSurface) {
                                    mHaveEglSurface = true;
                                    createEglSurface = true;
                                    sizeChanged = true;
                                    w = mWidth;
                                    h = mHeight;
                                }

                                // 判断是否需要重置size
                                if (mHaveEglSurface && mSizeChanged) {
                                    sizeChanged = true;
                                    mSizeChanged = false;
                                    w = mWidth;
                                    h = mHeight;
                                }

                                break;
                            }
                            sGlThreadManager.wait();
                        }
                    }

                    if (createEglSurface) {
                        mEglHelper.createSurface();
                        createEglSurface = false;
                    }

                    if (createEglContext) {
                        GLTextureView view = mGLSurfaceViewWeakRef.get();
                        view.mRenderer.onSurfaceCreated(null, null);
                        createEglContext = false;
                    }

                    if (sizeChanged) {
                        GLTextureView view = mGLSurfaceViewWeakRef.get();
                        view.mRenderer.onSurfaceChanged(null, w, h);
                        sizeChanged = false;
                    }

                    GLTextureView view = mGLSurfaceViewWeakRef.get();
                    view.mRenderer.onDrawFrame(null);
                    mEglHelper.swap();
                }
            } finally {
                synchronized (sGlThreadManager) {
                    stopEglSurfaceLocked();
                    stopEglContextLocked();
                }
            }
        }

        private void onWindowResize(int w, int h) {
            synchronized (sGlThreadManager) {
                mWidth = w;
                mHeight = h;

                if (Thread.currentThread() == this) {
                    return;
                }

                mSizeChanged = true;
                sGlThreadManager.notifyAll();
            }
        }

        private void surfaceCreated(int w, int h) {
            synchronized (sGlThreadManager) {
                mWidth = w;
                mHeight = h;
                mHasSurface = true;
                sGlThreadManager.notifyAll();
                while ((!mExited) && (mWaitingForSurface)) {
                    try {
                        sGlThreadManager.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void surfaceDestroyed() {
            synchronized (sGlThreadManager) {
                mHasSurface = false;
                sGlThreadManager.notifyAll();
                while ((!mExited) && (!mWaitingForSurface)) {
                    try {
                        sGlThreadManager.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        public void onPause() {
            synchronized (sGlThreadManager) {
                mRequestPaused = true;
                sGlThreadManager.notifyAll();
                while ((!mExited) && (!mPaused)) {
                    try {
                        sGlThreadManager.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void onResume() {
            synchronized (sGlThreadManager) {
                mRequestPaused = false;
                sGlThreadManager.notifyAll();
                while ((!mExited) && (mPaused)) {
                    try {
                        sGlThreadManager.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 请求退出并需要等待
         */
        private void requestExitAndWait() {

            synchronized (sGlThreadManager) {
                mShouldExit = true;
                sGlThreadManager.notifyAll();
                while (!mExited) {
                    try {
                        sGlThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        private void stopEglSurfaceLocked() {
            if (mHaveEglSurface) {
                mHaveEglSurface = false;
                mEglHelper.destroySurface();
            }
        }

        private void stopEglContextLocked() {
            if (mHaveEglContext) {
                mEglHelper.finish();
                mHaveEglContext = false;
                sGlThreadManager.releaseEglContextLocked(this);
            }
        }

        private boolean readyToDraw() {
            return mHasSurface && !mPaused && (mWidth > 0) && (mHeight > 0);
        }

        private int mWidth;
        private int mHeight;

        // 退出
        private boolean mShouldExit;
        private boolean mExited;

        // 生命周期
        private boolean mRequestPaused;
        private boolean mPaused;

        private boolean mHaveEglContext;
        private boolean mHaveEglSurface;

        private boolean mHasSurface; // 是否有surface
        private boolean mWaitingForSurface; // 等待surface

        private boolean mSizeChanged = true; // 注意添加surface也会调用

        private EglHelper mEglHelper;
        private WeakReference<GLTextureView> mGLSurfaceViewWeakRef;
    }

    /**
     * 管理EGL的管理类
     */
    private static class EglHelper {

        EglHelper(WeakReference<GLTextureView> glSurfaceViewWeakRef) {
            mGLSurfaceViewWeakRef = glSurfaceViewWeakRef;
        }

        void start() {
            // 创建display
            mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetdisplay failed : " +
                        GLUtils.getEGLErrorString(EGL14.eglGetError()));
            }

            // 配置参数
            int[] configAttributes = {
                    EGL14.EGL_BUFFER_SIZE, 32,  // 颜色缓存区
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_DEPTH_SIZE, 16,  // 深度缓存区
                    EGL14.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, // 渲染布局格式
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,  // 渲染窗口
                    EGL14.EGL_NONE
            };
            int[] numConfigs = new int[1];
            EGLConfig[] configs = new EGLConfig[1];
            if (!EGL14.eglChooseConfig(mEglDisplay, configAttributes, 0, configs, 0,
                    configs.length, numConfigs, 0)) {
                throw new RuntimeException("eglChooseConfig failed : " +
                        GLUtils.getEGLErrorString(EGL14.eglGetError()));
            }
            mEglConfig = configs[0];

            //创建EGL Context
            int[] contextAttribs = {
                    EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
            };
            mEglContext = EGL14.eglCreateContext(mEglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
            if (mEglContext == EGL14.EGL_NO_CONTEXT) {
                throw new RuntimeException("eglCreateContext failed ：" +
                        GLUtils.getEGLErrorString(EGL14.eglGetError()));
            }

            mEglSurface = null;
        }

        private void finish() {
            if (mEglDisplay != null) {
                EGL14.eglTerminate(mEglDisplay);
                mEglDisplay = null;
            }
        }

        private boolean createSurface() {
            destroySurfaceImp();

            // 创建surface
            int[] surfaceAttribs = {
                    EGL14.EGL_NONE
            };
            mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, mGLSurfaceViewWeakRef.get().getSurfaceTexture(), surfaceAttribs, 0);
            if (mEglSurface == EGL14.EGL_NO_SURFACE || mEglContext == EGL14.EGL_NO_CONTEXT) {
                int error = EGL14.eglGetError();
                if (error == EGL14.EGL_BAD_NATIVE_WINDOW) {
                    throw new RuntimeException("eglCreateWindowSurface returned  EGL_BAD_NATIVE_WINDOW. ");
                }
                throw new RuntimeException("eglCreateWindowSurface failed : " +
                        GLUtils.getEGLErrorString(EGL14.eglGetError()));
            }

            // 指定当前为活动的surface
            if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                throw new RuntimeException("eglMakeCurrent failed : " +
                        GLUtils.getEGLErrorString(EGL14.eglGetError()));
            }
            return true;
        }

        private void destroySurface() {
            destroySurfaceImp();
        }

        // 刷新到屏幕
        private int swap() {
            if (!EGL14.eglSwapBuffers(mEglDisplay, mEglSurface)) {
                return EGL14.eglGetError();
            }
            return EGL10.EGL_SUCCESS;
        }

        private void destroySurfaceImp() {
            if (mEglSurface != null && mEglSurface != EGL14.EGL_NO_SURFACE) {
                EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT);
                mEglSurface = null;
            }
        }

        WeakReference<GLTextureView> mGLSurfaceViewWeakRef;
        EGLDisplay mEglDisplay;
        EGLConfig mEglConfig;
        EGLContext mEglContext;
        EGLSurface mEglSurface;
    }

    private static class GLThreadManager {

        private synchronized void threadExiting(GlThread thread) {
            thread.mExited = true;
            notifyAll();
        }


        private void releaseEglContextLocked(GlThread thread) {
            notifyAll();
        }
    }

    private static final GLThreadManager sGlThreadManager = new GLThreadManager();
    private final WeakReference<GLTextureView> mThisWeakRef = new WeakReference<GLTextureView>(this);
    private GlThread mGlThread;
    private GLSurfaceView.Renderer mRenderer;
    private boolean mDetached;
}
