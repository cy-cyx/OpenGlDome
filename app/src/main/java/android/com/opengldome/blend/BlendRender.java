package android.com.opengldome.blend;

import android.com.opengldome.Application;
import android.com.opengldome.R;
import android.com.opengldome.utils.CommonUtils;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BlendRender implements GLSurfaceView.Renderer {

    private Context context;

    private int mWidth;
    private int mHeight;
    private int mTexture1;
    private int mTexture2;
    int mFragment;
    int mVertex;
    int mProgramObject;

    private FloatBuffer bPos;
    private final float[] sPos = {
            -1.0f, 1.f, 0f,      // 左上角
            -1.0f, -1.0f, 0f,    // 左下角
            1.0f, 1f, 0f,        // 右上角
            1.0f, -1.f, 0f       // 右下角
    };

    private FloatBuffer bCoord;
    private final float[] sCoord = {
            0f, 0f,             // 左上角
            0f, 1f,              // 左下角
            1f, 0f,              // 右上角
            1f, 1f              // 右下角
    };

    /**
     * 混合模式  fs * Cs op fd * Cd
     */
    @interface MixedMode {
        int MIXE_ZERO = GLES30.GL_ZERO; // 零
        int MIXE_ONE = GLES30.GL_ONE; // 壹
        int MIXE_SRC_COLOR = GLES30.GL_SRC_COLOR; // 输入像素
        int MIXE_ONE_MINUS_SRC_COLOR = GLES30.GL_ONE_MINUS_SRC_COLOR; // 1 - 输入像素
        int MIXE_SRC_ALPHA = GLES30.GL_SRC_ALPHA; // 输入像素透明度
        int MIXE_ONE_MINUS_SRC_ALPHA = GLES30.GL_ONE_MINUS_SRC_ALPHA; // 1 - 输入像素透明度
        int MIXE_DST_COLOR = GLES30.GL_DST_COLOR; // 目标像素
        int MIXE_ONE_MINUS_DST_COLOR = GLES30.GL_ONE_MINUS_DST_COLOR; // 目标像素
        int MIXE_DST_ALPHA = GLES30.GL_DST_ALPHA; // 目标像素透明度
        int MIXE_ONE_MINUS_DST_ALPHA = GLES30.GL_ONE_MINUS_DST_ALPHA; // 1 - 目标像素透明度
        int MIXE_CONSTANT_COLOR = GLES30.GL_CONSTANT_COLOR; // 常量颜色
        int MIXE_ONE_MINS_CONSTANT_COLOR = GLES30.GL_ONE_MINUS_CONSTANT_COLOR; // 1 - 常量颜色
        int MIXE_CONSTANT_ALPHA = GLES30.GL_CONSTANT_ALPHA; // 常量透明度
        int MIXE_ONE_MINUS_CONNSTANT_ALPHA = GLES30.GL_ONE_MINUS_CONSTANT_ALPHA; // 1 - 常量透明度
        int MIXE_SRC_ALPHA_SATURATE = GLES30.GL_SRC_ALPHA_SATURATE;  // mix(输入常量透明，1 - 目标常量透明度)
    }

    int mNixeModeSrc = MixedMode.MIXE_CONSTANT_COLOR;
    int mMixeModeDst = MixedMode.MIXE_CONSTANT_COLOR;

    @interface Sparate {
        int Sparate_ADD = GLES30.GL_FUNC_ADD; // 累加
        int Sparate_SUBTRACT = GLES30.GL_FUNC_SUBTRACT; // 输入片段 - 缓冲区
        int Sparate_REVERSE_SUBTRACT = GLES30.GL_FUNC_REVERSE_SUBTRACT; // 缓冲区 - 输入
        int Sparate_MIN = GLES30.GL_MIN; // 取最小
        int Sparate_MAX = GLES30.GL_MAX; // 取最大
    }

    int mSparate = Sparate.Sparate_SUBTRACT;

    public BlendRender(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        bPos = CommonUtils.fToB(sPos);
        bCoord = CommonUtils.fToB(sCoord);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glEnable(GLES30.GL_TEXTURE_2D);//启用纹理贴图
        // 混合
        GLES30.glEnable(GLES30.GL_BLEND);
        // 设置常量像素
        GLES30.glBlendColor(.3f, .3f, 3.f, 1.f);

        mWidth = width;
        mHeight = height;

        mFragment = CommonUtils.loadShader(Application.getInstance(), GLES30.GL_FRAGMENT_SHADER, "blend/fragment.glsl");
        mVertex = CommonUtils.loadShader(Application.getInstance(), GLES30.GL_VERTEX_SHADER, "blend/vertex.glsl");

        mProgramObject = GLES30.glCreateProgram();
        GLES30.glAttachShader(mProgramObject, mFragment);
        GLES30.glAttachShader(mProgramObject, mVertex);

        GLES30.glLinkProgram(mProgramObject);

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, bPos);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, bCoord);
        GLES30.glEnableVertexAttribArray(1);

        mTexture1 = CommonUtils.newTexture(mWidth, mHeight, BitmapFactory.decodeResource(context.getResources(), R.drawable.pic1));
        mTexture2 = CommonUtils.newTexture(mWidth, mHeight, BitmapFactory.decodeResource(context.getResources(), R.drawable.pic2));
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        gl.glViewport(0, 0, mWidth, mHeight);

        if (mSparate == Sparate.Sparate_MIN) {
            gl.glClearColor(1.f, 1.f, 1.f, 1.f);
        }

        // 设置混合
        GLES30.glBlendFunc(mNixeModeSrc, mMixeModeDst);
        GLES30.glBlendEquation(mSparate);

        // 画第一层
        GLES30.glUseProgram(mProgramObject);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTexture1);
        GLES30.glUniform1i(2, mTexture1);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        // 画第二层
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTexture2);
        GLES30.glUniform1i(2, mTexture2);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
    }
}
