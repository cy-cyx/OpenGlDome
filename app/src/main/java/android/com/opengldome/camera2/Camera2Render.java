package android.com.opengldome.camera2;

import android.com.opengldome.Application;
import android.com.opengldome.beauty.GLFrameBuffer;
import android.com.opengldome.beauty.LookupTableFilter;
import android.com.opengldome.egl.GLTextureView;
import android.com.opengldome.utils.CommonUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Message;
import android.util.Size;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

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
        lookupTableFilter.setUseOes(true);
        lookupTableFilter.setGlFrameBuffer(null);
        lookupTableFilter.onDraw(glFrameBuffer.getTexture(), width, height);
    }

    public void onOpenCamera(String id, Size size, int angle) {
        oesFilter.onOpenCamera(id, size, width, height, angle);
        oesSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
    }

    /**
     * 点击保存后，图片处理
     *
     * @param data       镜头数据
     * @param front      是否是前摄
     * @param bmpWidth   数据宽
     * @param bmpHeight  数据高
     * @param mainHandle Activity的Handle
     */
    public void dealPicture(final byte[] data, final boolean front, final int bmpWidth, final int bmpHeight, final Camera2Activity.MainHandle mainHandle) {
        Runnable dealRun = new Runnable() {
            @Override
            public void run() {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap targetBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                // 预先处理bitmap 前置翻转 裁剪
                int x = 0;
                int y = 0;
                int cropWidth = 0;
                int cropHeight = 0;

                if ((float) bmpWidth / (float) bmpHeight > (float) width / (float) height) {
                    cropHeight = bmpHeight;
                    cropWidth = (int) (bmpHeight * (float) width / (float) height);
                    x = (int) ((bmpWidth - cropWidth) / 2.f);
                } else {
                    cropWidth = bmpWidth;
                    cropHeight = (int) (bmpWidth * (float) height / (float) width);
                    y = (int) ((bmpHeight - cropHeight) / 2.f);
                }

                Matrix matrix = new Matrix();
                if (front) {
                    matrix.setScale(-1, 1);
                }
                Bitmap cropBitmap = Bitmap.createBitmap(targetBitmap, x, y, cropWidth, cropHeight, matrix, false);

                CommonUtils.newTexture(3, cropBitmap);


                // draw
                GLFrameBuffer frameBuffer = new GLFrameBuffer(2, cropWidth, cropHeight);

                lookupTableFilter.setCurAlpha(0.5f);
                lookupTableFilter.setUseOes(false);
                lookupTableFilter.setGlFrameBuffer(frameBuffer);
                lookupTableFilter.onDraw(3, cropWidth, cropHeight);
                GLES30.glFinish();

                final ByteBuffer buffer = ByteBuffer.allocateDirect(cropWidth * cropHeight * 4);
                buffer.rewind();
                GLES30.glReadPixels(0, 0, cropWidth, cropHeight, GLES30.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

                Bitmap bitmap = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888);
                buffer.rewind();
                bitmap.copyPixelsFromBuffer(buffer);

                Message.obtain(mainHandle, Camera2Activity.MainHandle.MSG_DEALPIC_SUCCESS, bitmap).sendToTarget();
            }
        };
        GLTextureView glTextureView = glTextureViewWeakReference.get();
        if (glTextureView != null) {
            glTextureView.execute(dealRun);
        }
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
