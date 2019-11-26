package android.com.opengldome.beauty;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.utils.CommonUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * create by cy
 * time : 2019/11/26
 * version : 1.0
 * Features : 颜色滤镜
 */
public class BeautyRender implements GLSurfaceView.Renderer {

    private LookupTableFilter lookupTableFilter;

    private int width;
    private int height;

    private int sourceTexture;

    public BeautyRender() {
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        lookupTableFilter = new LookupTableFilter(Application.getInstance());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outConfig = Bitmap.Config.ARGB_8888;
        sourceTexture = CommonUtils.newTexture(0, BitmapFactory.decodeResource(Application.getInstance().getResources(), R.drawable.pic3, options));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        lookupTableFilter.onDraw(sourceTexture, width, height);
    }

    public void setCurAlpha(float alpha) {
        lookupTableFilter.setCurAlpha(alpha);
    }
}
