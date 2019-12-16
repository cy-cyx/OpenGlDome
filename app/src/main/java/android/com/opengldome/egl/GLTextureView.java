package android.com.opengldome.egl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

/**
 * create by cy
 * time : 2019/11/28
 * version : 1.0
 * Features : TextureView中维护着一条Gl线程
 */
public class GLTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private GLThread glThread;
    private GlRender glRender;

    public GLTextureView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }

    public void setRender(GlRender glRender) {
        if (this.glRender != null)
            throw new RuntimeException("请不要重复设置Render");
        this.glRender = glRender;
        glThread = new GLThread();
        glThread.setGlRender(glRender);
        glThread.start();
    }

    /**
     * 外纹理可用时请求渲染
     */
    public void requestRender() {
        if (glThread != null)
            glThread.requestRender();
    }

    /**
     * 在gl线程执行特定的任务
     */
    public void execute(Runnable runnable) {
        if (glThread != null)
            glThread.execute(runnable);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        glThread.createSurface(surface);
        glThread.surfaceChanged(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (glThread != null)
            glThread.surfaceChanged(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (glThread != null)
            glThread.surfaceDestroy();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (glRender != null && glThread == null) {
            glThread = new GLThread();
            glThread.setGlRender(glRender);
            glThread.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        glThread.quit();
        super.onDetachedFromWindow();
    }

    public interface GlRender {
        public void onSurfaceCreate();

        public void onSurfaceChange(int width, int height);

        public void onDraw();
    }
}
