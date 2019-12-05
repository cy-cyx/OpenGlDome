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
     * 通过当前的CameraId设置正确预览方向和比例（投影 - 视图 - 模式 矩阵）
     */
    public void onOpenCamera(String id, Size size, int width, int height, int angle) {
        if (id.equals("0")) {

            // 正确的显示大小
            int targetViewWidth = width;
            int targetViewHeight = height;

            if (angle == 90 || angle == 270) {
                targetViewWidth = height;
                targetViewHeight = width;
            }

            int bufferWidth = size.getWidth();
            int bufferHeight = size.getHeight();

            float x = 1f;
            float y = 1f;
            if ((float) bufferWidth / (float) bufferHeight >= (float) targetViewWidth / (float) targetViewHeight) {
                float tempWidth = (float) targetViewHeight / (float) bufferHeight * (float) bufferWidth;
                x = tempWidth / bufferWidth;
            } else {
                float tempHeight = (float) targetViewWidth / (float) bufferWidth * (float) bufferHeight;
                y = tempHeight / bufferHeight;
            }


            float[] mProjectionMatrix = new float[16];
            float[] mViewMatrax = new float[16];
            float[] mModelMatrax = new float[16];

            float[] mPv = new float[16];

            float[] rotate = new float[16];
            float[] scale = new float[16];

            Matrix.setIdentityM(matrix, 0);
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -1, 1, 3.f, 7f);
            Matrix.setLookAtM(mViewMatrax, 0, 0, 0, -3.f, 0f, 0f, 0f, 0f, 1f, 0f);
            Matrix.multiplyMM(mPv, 0, mProjectionMatrix, 0, mViewMatrax, 0);

            Matrix.setIdentityM(rotate, 0);
            Matrix.rotateM(rotate, 0, angle, 0, 0, 1);
            Matrix.setIdentityM(scale, 0);
            Matrix.scaleM(scale, 0, x, y, 1f);
            Matrix.multiplyMM(mModelMatrax, 0, rotate, 0, scale, 0);

            Matrix.multiplyMM(matrix, 0, mPv, 0, mModelMatrax, 0);

        } else if (id.equals("1")) {

            // 正确的显示大小
            int targetViewWidth = width;
            int targetViewHeight = height;

            if (angle == 90 || angle == 270) {
                targetViewWidth = height;
                targetViewHeight = width;
            }

            int bufferWidth = size.getWidth();
            int bufferHeight = size.getHeight();

            float x = 1f;
            float y = 1f;
            if ((float) bufferWidth / (float) bufferHeight >= (float) targetViewWidth / (float) targetViewHeight) {
                float tempWidth = (float) targetViewHeight / (float) bufferHeight * (float) bufferWidth;
                x = tempWidth / bufferWidth;
            } else {
                float tempHeight = (float) targetViewWidth / (float) bufferWidth * (float) bufferHeight;
                y = tempHeight / bufferHeight;
            }


            float[] mProjectionMatrix = new float[16];
            float[] mViewMatrax = new float[16];
            float[] mModelMatrax = new float[16];

            float[] mPv = new float[16];

            float[] rotate = new float[16];
            float[] scale = new float[16];

            Matrix.setIdentityM(matrix, 0);
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -1, 1, 3.f, 7f);
            Matrix.setLookAtM(mViewMatrax, 0, 0, 0, 3.f, 0f, 0f, 0f, 0f, -1f, 0f);
            Matrix.multiplyMM(mPv, 0, mProjectionMatrix, 0, mViewMatrax, 0);

            Matrix.setIdentityM(rotate, 0);
            Matrix.rotateM(rotate, 0, angle, 0, 0, 1);
            Matrix.setIdentityM(scale, 0);
            Matrix.scaleM(scale, 0, x, y, 1f);
            Matrix.multiplyMM(mModelMatrax, 0, rotate, 0, scale, 0);

            Matrix.multiplyMM(matrix, 0, mPv, 0, mModelMatrax, 0);
        }
    }
}
