package android.com.opengldome.camera2;

import android.com.opengldome.Application;
import android.com.opengldome.beauty.GLFrameBuffer;
import android.com.opengldome.beauty.LookupTableFilter;
import android.com.opengldome.egl.GLTextureView;
import android.com.opengldome.utils.CommonUtils;
import android.com.opengldome.utils.WHView;
import android.graphics.SurfaceTexture;

import java.lang.ref.WeakReference;

/**
 * create by cy
 * time : 2019/11/28
 * version : 1.0
 * Features : 用获取相机数据，并矫正方向 
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

    private Camera2RenderCallBack camera2RenderCallBack;

    public void setGlTextureView(GLTextureView glTextureView) {
        this.glTextureViewWeakReference = new WeakReference<>(glTextureView);
    }

    @Override
    public void onSurfaceCreate() {
        initOES();
        glFrameBuffer = new GLFrameBuffer(2, (int) WHView.getViewWidth(), (int) WHView.getViewHeight());
        oesFilter = new OESFilter(Application.getInstance());
        lookupTableFilter = new LookupTableFilter(Application.getInstance());
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
        if (camera2RenderCallBack != null)
            camera2RenderCallBack.onEOSAvailable(oesSurfaceTexture);
    }

    @Override
    public void onSurfaceChange(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDraw() {
        oesFilter.setGlFrameBuffer(glFrameBuffer);
        oesFilter.onDraw(oesTexture, width, height);
        lookupTableFilter.setCurAlpha(1);
        lookupTableFilter.onDraw(glFrameBuffer.getTexture(), width, height);
    }

    public void setCamera2RenderCallBack(Camera2RenderCallBack callBack) {
        this.camera2RenderCallBack = callBack;
    }

    public interface Camera2RenderCallBack {
        public void onEOSAvailable(SurfaceTexture surfaceTexture);
    }
}
