package android.com.opengldome.beauty;

import android.com.opengldome.R;
import android.com.opengldome.utils.BitmapUtils;
import android.com.opengldome.utils.CommonUtils;
import android.content.Context;
import android.opengl.GLES30;

/**
 * create by cy
 * time : 2019/11/26
 * version : 1.0
 * Features : 颜色滤镜查色表
 */
public class LookupTableFilter extends AFilter {

    private int colorTable;
    private int alpha;

    private int colorTableTexture;
    private float curAlpha = 0.f;

    public LookupTableFilter(Context context) {
        super(context, R.raw.lookup_table_vert, R.raw.lookup_table_frag);
        initColorTable(context);
    }

    private void initColorTable(Context context) {
        colorTableTexture = CommonUtils.newTexture(0, BitmapUtils.getBitmapByAsset(context, "colorTable0.png"));
    }

    @Override
    protected void linkLocation() {
        super.linkLocation();
        alpha = GLES30.glGetUniformLocation(program, "uAlpha");
        colorTable = GLES30.glGetUniformLocation(program, "uLookupTexture");
    }

    @Override
    protected void bindValue() {
        super.bindValue();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, colorTableTexture);
        GLES30.glUniform1i(colorTable, 2);

        GLES30.glUniform1f(alpha, curAlpha);
    }

    @Override
    protected void unBindValue() {
        super.unBindValue();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    public void setCurAlpha(float curAlpha) {
        this.curAlpha = curAlpha;
    }
}
