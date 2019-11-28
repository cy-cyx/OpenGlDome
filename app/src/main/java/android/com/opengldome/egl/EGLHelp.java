package android.com.opengldome.egl;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLUtils;

import javax.microedition.khronos.egl.EGL10;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;

/**
 * create by cy
 * time : 2019/11/28
 * version : 1.0
 * Features :
 */
public class EGLHelp {

    EGLDisplay mEglDisplay;
    EGLConfig mEglConfig;
    EGLContext mEglContext;
    EGLSurface mEglSurface;

    public void start() {
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

    public boolean createSurface(SurfaceTexture surfaceTexture) {
        destroySurfaceImp();

        // 创建surface
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surfaceTexture, surfaceAttribs, 0);
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

    // 刷新到屏幕
    public int swap() {
        if (!EGL14.eglSwapBuffers(mEglDisplay, mEglSurface)) {
            return EGL14.eglGetError();
        }
        return EGL10.EGL_SUCCESS;
    }

    public void destroySurface() {
        destroySurfaceImp();
    }

    private void destroySurfaceImp() {
        if (mEglSurface != null && mEglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT);
            mEglSurface = null;
        }
    }

    public void finish() {
        if (mEglDisplay != null) {
            EGL14.eglTerminate(mEglDisplay);
            mEglDisplay = null;
        }
    }
}
