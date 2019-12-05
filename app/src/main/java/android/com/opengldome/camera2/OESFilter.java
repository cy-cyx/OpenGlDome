package android.com.opengldome.camera2;

import android.com.opengldome.R;
import android.com.opengldome.beauty.AFilter;
import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Size;

/**
 * create by cy
 * time : 2019/11/29
 * version : 1.0
 * Features : 相机返回的数据的第一层处理
 * 1、旋转成正确方向
 */
public class OESFilter extends AFilter {

    private int uMatrix;
    private volatile float[] matrix = new float[16];

    public OESFilter(Context context) {
        super(context, R.raw.oes_vert, R.raw.oes_frag);
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

    /**
     * 通过当前的CameraId设置正确预览方向
     */
    public void onOpenCamera(String id, Size size, int width, int height, int angle) {
        if (id.equals("0")) {

            // 矫正角度
            int upX = 0;
            int upY = 0;
            int upZ = 0;
            if (angle == 0) {
                upY = 1;
            } else if (angle == 90) {
                upX = 1;
            } else if (angle == 180) {
                upY = -1;
            } else if (angle == 270) {
                upX = -1;
            }

            // 正确的显示大小
            int targetViewWidth = width;
            int targetViewHeight = height;

            if (angle == 90 || angle == 270) {
                targetViewWidth = height;
                targetViewHeight = width;
            }

            int bufferWidth = size.getWidth();
            int bufferHeight = size.getHeight();

            float left = -1;
            float right = 1;
            float bottom = -1;
            float top = 1;
            if ((float) targetViewWidth / (float) targetViewHeight >= (float) bufferWidth / (float) bufferHeight) {
                float tempHeight = (float) targetViewWidth / (float) bufferWidth * (float) bufferHeight;
                top = (float) tempHeight / (float) bufferHeight;
                bottom = -(float) tempHeight / (float) bufferHeight;
            } else {
                float tempWidth = (float) targetViewHeight / (float) bufferHeight * (float) bufferWidth;
                right = (float) tempWidth / (float) bufferWidth;
                left = -(float) tempWidth / (float) targetViewWidth;
            }

            float[] mProjectionMatrix = new float[16];
            float[] mViewMatrax = new float[16];
            Matrix.setIdentityM(matrix, 0);
            Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, 4f, 7f);
            Matrix.setLookAtM(mViewMatrax, 0, 0, 0, -4.00001f, 0f, 0f, 0f, upX, upY, upZ);
            Matrix.multiplyMM(matrix, 0, mProjectionMatrix, 0, mViewMatrax, 0);
        } else if (id.equals("1")) {
            float[] mProjectionMatrix = new float[16];
            float[] mViewMatrax = new float[16];
            Matrix.setIdentityM(matrix, 0);
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -1, 1, 4f, 7f);
            Matrix.setLookAtM(mViewMatrax, 0, 0, 0, -4.00001f, 0f, 0f, 0f, 0.f, 1.f, 0.0f);
            Matrix.multiplyMM(matrix, 0, mProjectionMatrix, 0, mViewMatrax, 0);
        }
    }
}
