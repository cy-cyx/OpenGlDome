package android.com.opengldome.camera2;

import android.com.opengldome.R;
import android.com.opengldome.beauty.AFilter;
import android.com.opengldome.utils.WHView;
import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

/**
 * create by cy
 * time : 2019/11/29
 * version : 1.0
 * Features : 相机返回的数据的第一层处理
 * 1、旋转成正确方向
 */
public class OESFilter extends AFilter {

    private int uMatrix;
    private float[] matrix = new float[16];

    public OESFilter(Context context) {
        super(context, R.raw.oes_vert, R.raw.oes_frag);

        float[] mProjectionMatrix = new float[16];
        float[] mViewMatrax = new float[16];
        Matrix.setIdentityM(matrix, 0);
        Matrix.frustumM(mProjectionMatrix, 0, -WHView.getViewWidth() / WHView.getViewHeight(), WHView.getViewWidth() / WHView.getViewHeight(), -1, 1, 4f, 7f);
        Matrix.setLookAtM(mViewMatrax, 0, 0, 0, -4.00001f, 0f, 0f, 0f, 1.f, 0f, 0.0f);
        Matrix.multiplyMM(matrix, 0, mProjectionMatrix, 0, mViewMatrax, 0);
    }

    @Override
    protected void linkLocation() {
        super.linkLocation();
        uMatrix = GLES30.glGetUniformLocation(program, "uMatrix");
    }

    @Override
    protected void bindValue() {
        super.bindValue();
        GLES30.glUniformMatrix4fv(uMatrix, 1, false, matrix, 0);
    }
}
