package android.com.opengldome.watermark;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.beauty.AFilter;
import android.com.opengldome.utils.CommonUtils;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import static android.opengl.GLES10.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES10.GL_SRC_ALPHA;

/**
 * create by caiyx in 2021/3/22
 * <p>
 * 画水印
 */
class WaterMarkFilter extends AFilter {

    private int uMatrix;
    private volatile float[] matrix = new float[16];

    private int mTexture;

    private final float[] sbmPos = {
            1308 / 2022f, -1874 / 2022f, 0f,      // 左上角
            1308 / 2022f, -1982 / 2022f, 0f,    // 左下角
            1982 / 2022f, -1874 / 2022f, 0f,        // 右上角
            1982 / 2022f, -1982 / 2022f, 0f       // 右下角
    };
    private FloatBuffer bmPos;

    WaterMarkFilter(Context context) {
        super(context, R.raw.watermark_vert, R.raw.watermark_frag);
        bmPos = CommonUtils.fToB(sbmPos);
        Matrix.setIdentityM(matrix, 0);

        mTexture = CommonUtils.newTexture(1, BitmapFactory.decodeResource(Application.getInstance().getResources(), R.drawable.water_mark_674_108));
    }

    public void upDataMatrix(int wight, int height, int rotation) {
        float[] transfer = new float[16];
        float[] scale = new float[16];
        float[] rotater = new float[16];


        if (rotation == 0) {
            // 先移到到中间
            Matrix.setIdentityM(transfer, 0);
            Matrix.translateM(transfer, 0, 0, 1928 / 2022f, 0);

            // 缩放
            Matrix.setIdentityM(scale, 0);
            Matrix.scaleM(scale, 0, 1, wight / (float) height, 1);

            Matrix.multiplyMM(matrix, 0, scale, 0, transfer, 0);

            // 再挪到原来的地方
            Matrix.setIdentityM(transfer, 0);
            Matrix.translateM(transfer, 0, 0, -(2022 - 40 - 108 * wight / (float) height / 2) / 2022f, 0);

            Matrix.multiplyMM(matrix, 0, transfer, 0, matrix, 0);
        } else if (rotation == 90) {
            // 先旋转
            Matrix.setIdentityM(rotater, 0);
            Matrix.rotateM(rotater, 0, rotation, 0, 0, 1);

            // 先移到到中间
            Matrix.setIdentityM(transfer, 0);
            Matrix.translateM(transfer, 0, -1928 / 2022f, 0, 0);

            Matrix.multiplyMM(matrix, 0, transfer, 0, rotater, 0);

            // 缩放
            Matrix.setIdentityM(scale, 0);
            Matrix.scaleM(scale, 0, height / (float) wight, 1, 1);

            Matrix.multiplyMM(matrix, 0, scale, 0, matrix, 0);

            // 再挪到原来的地方
            Matrix.setIdentityM(transfer, 0);
            Matrix.translateM(transfer, 0, (2022 - 40 - 108 * height / (float) wight / 2) / 2022f, 0, 0);

            Matrix.multiplyMM(matrix, 0, transfer, 0, matrix, 0);
        }
    }

    @Override
    protected void linkLocation() {
        super.linkLocation();
        uMatrix = GLES30.glGetUniformLocation(program, "uMatrix");
    }

    public void onDraw(int width, int height) {
        super.onDraw(mTexture, width, height);
    }

    @Override
    protected void useProgram() {
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        super.useProgram();
    }

    @Override
    protected void bindValue() {
        super.bindValue();
        GLES30.glVertexAttribPointer(position, 3, GLES30.GL_FLOAT, false, 0, bmPos);
        GLES30.glEnableVertexAttribArray(position);
        GLES30.glVertexAttribPointer(texcoord, 2, GLES30.GL_FLOAT, false, 0, bCoord);
        GLES30.glEnableVertexAttribArray(texcoord);
        GLES30.glUniformMatrix4fv(uMatrix, 1, false, matrix, 0);
    }


    @Override
    protected void clear() {
    }


    @Override
    protected void clearProgram() {
        super.clearProgram();
        GLES30.glDisable(GLES30.GL_BLEND);
    }
}
