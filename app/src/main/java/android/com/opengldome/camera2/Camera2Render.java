package android.com.opengldome.camera2;

import android.com.opengldome.Application;
import android.com.opengldome.beauty.GLFrameBuffer;
import android.com.opengldome.beauty.LookupTableFilter;
import android.com.opengldome.egl.GLTextureView;
import android.com.opengldome.utils.CommonUtils;
import android.graphics.SurfaceTexture;
import android.util.Size;

import java.lang.ref.WeakReference;

/**
 * create by cy
 * time : 2019/11/28
 * version : 1.0
 * Features :
 */
public class Camera2Render implements GLTextureView.GlRender {

    private OESFilter oesFilter;
    private LookupTableFilter lookupTableFilter;
    private GLFrameBuffer glFrameBuffer;
    private int oesTexture;
    private SurfaceTexture oesSurfaceTexture;
    private int width;
    private int height;
    private WeakReference<GLTextureView> glTextureViewWeakReference;
    private boolean create = false;

    private Camera2RenderCallBack camera2RenderCallBack;

    public void setGlTextureView(GLTextureView glTextureView) {
        this.glTextureViewWeakReference = new WeakReference<>(glTextureView);
    }

    @Override
    public void onSurfaceCreate() {
        create = true;
        oesFilter = new OESFilter(Application.getInstance());
        lookupTableFilter = new LookupTableFilter(Application.getInstance());
        lookupTableFilter.setUseOes(true);
        initOES();
    }

    private void initOES() {
        oesTexture = CommonUtils.createTextureOES();
        oesSurfaceTexture = new SurfaceTexture(oesTexture);
        oesSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                try {
                    surfaceTexture.updateTexImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                GLTextureView glTextureView = glTextureViewWeakReference.get();
                if (glTextureView != null)
                    glTextureView.requestRender();
            }
        });
    }

    /**
     * {@link android.view.TextureView.SurfaceTextureListener#onSurfaceTextureAvailable}默认调用同一个接口
     * 当{@link #create}为true
     */
    @Override
    public void onSurfaceChange(int width, int height) {
        this.width = width;
        this.height = height;
        if (create) {
            glFrameBuffer = new GLFrameBuffer(2, width, height);
            if (camera2RenderCallBack != null)
                camera2RenderCallBack.onEOSAvailable(oesSurfaceTexture);
            create = false;
        }
    }

    @Override
    public void onDraw() {
        oesFilter.setGlFrameBuffer(glFrameBuffer);
        oesFilter.onDraw(oesTexture, width, height);
        lookupTableFilter.setCurAlpha(0.5f);
        lookupTableFilter.onDraw(glFrameBuffer.getTexture(), width, height);
    }

    public void onOpenCamera(String id, Size size, int angle) {
        oesFilter.onOpenCamera(id, size, width, height, angle);
        oesSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
    }

    public void setCamera2RenderCallBack(Camera2RenderCallBack callBack) {
        this.camera2RenderCallBack = callBack;
    }

    public interface Camera2RenderCallBack {
        public void onEOSAvailable(SurfaceTexture surfaceTexture);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
