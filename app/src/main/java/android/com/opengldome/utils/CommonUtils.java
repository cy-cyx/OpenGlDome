package android.com.opengldome.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 基本的通用方法
 */
public class CommonUtils {

    public static FloatBuffer fToB(float[] floats) {
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(floats.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        floatBuffer.put(floats).position(0);
        return floatBuffer;
    }

    public static ByteBuffer bToB(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder());
        byteBuffer.position(0);
        return byteBuffer;
    }

    /**
     * 创建Shader资源
     *
     * @param type      shader类型
     * @param shaderSrc 资源路径
     * @return -1为失败状态
     */
    public static int loadShader(Context context, int type, String shaderSrc) {
        int shader;
        int[] compiled = new int[1];
        shader = GLES30.glCreateShader(type);
        if (shader == 0) {
            Log.e("xx", "获得Shader资源id失败");
            return -1;
        }
        GLES30.glShaderSource(shader, uRes(context.getResources(), shaderSrc));
        GLES30.glCompileShader(shader);
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String s = GLES30.glGetShaderInfoLog(shader);
            GLES30.glDeleteShader(shader);
            Log.e("xx", "Shader联接资源失败,ShaderInfoLog: " + s);
            return -1;
        }
        return shader;
    }

    /**
     * 新建一个新纹理
     *
     * @param width  宽
     * @param height 高
     */
    public static int newTexture(int width, int height, Bitmap bitmap) {
        int texture;
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        texture = textures[0];
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_GREATER);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_GREATER);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        return texture;
    }

    private static String uRes(Resources mRes, String path) {
        StringBuilder result = new StringBuilder();
        try {
            InputStream is = mRes.getAssets().open(path);
            int ch;
            byte[] buffer = new byte[1024];
            while (-1 != (ch = is.read(buffer))) {
                result.append(new String(buffer, 0, ch));
            }
        } catch (Exception e) {
            return null;
        }
        return result.toString().replaceAll("\\r\\n", "\n");
    }
}
