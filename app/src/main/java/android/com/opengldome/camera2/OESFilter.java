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
 * 2、修正成正确的比例
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
                float tempHeight = (float) bufferWidth / (float) targetViewWidth * (float) targetViewHeight;
                y = tempHeight / bufferHeight;
            } else {
                float tempWidth = (float) bufferHeight / (float) targetViewHeight * (float) targetViewWidth;
                x = tempWidth / bufferWidth;
            }

            float[] rotate = new float[16];
            float[] scale = new float[16];

            // 注意:左乘问题 先旋转成正确的方向 再缩放
            Matrix.setIdentityM(rotate, 0);
            Matrix.rotateM(rotate, 0, 360 - angle, 0, 0, 1);
            Matrix.setIdentityM(scale, 0);
            Matrix.scaleM(scale, 0, x, y, 1f);
            Matrix.multiplyMM(matrix, 0, scale, 0, rotate, 0);
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
                float tempHeight = (float) bufferWidth / (float) targetViewWidth * (float) targetViewHeight;
                y = tempHeight / bufferHeight;
            } else {
                float tempWidth = (float) bufferHeight / (float) targetViewHeight * (float) targetViewWidth;
                x = tempWidth / bufferWidth;
            }

            float[] rotate = new float[16];
            float[] scale = new float[16];

            Matrix.setIdentityM(rotate, 0);
            Matrix.rotateM(rotate, 0, angle, 0, 0, 1);
            Matrix.setIdentityM(scale, 0);
            Matrix.scaleM(scale, 0, x, y, 1f);
            Matrix.multiplyMM(matrix, 0, scale, 0, rotate, 0);
        }
    }
}
