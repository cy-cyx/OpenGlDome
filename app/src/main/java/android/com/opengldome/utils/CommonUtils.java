package android.com.opengldome.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import java.io.IOException;
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
            throw new RuntimeException("获得Shader资源id失败");
        }
        GLES30.glShaderSource(shader, uRes(context.getResources(), shaderSrc));
        GLES30.glCompileShader(shader);
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String s = GLES30.glGetShaderInfoLog(shader);
            GLES30.glDeleteShader(shader);
            throw new RuntimeException("Shader联接资源失败,ShaderInfoLog:" + s);
        }
        return shader;
    }

    /**
     * 创建Shader资源
     *
     * @param type      shader类型
     * @param shaderSrc 资源路径
     * @return -1为失败状态
     */
    public static int loadShader(Context context, int type, int shaderSrc) {
        int shader;
        int[] compiled = new int[1];
        shader = GLES30.glCreateShader(type);
        if (shader == 0) {
            throw new RuntimeException("获得Shader资源id失败");
        }
        GLES30.glShaderSource(shader, uRes(context.getResources(), shaderSrc));
        GLES30.glCompileShader(shader);
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String s = GLES30.glGetShaderInfoLog(shader);
            GLES30.glDeleteShader(shader);
            throw new RuntimeException("Shader联接资源失败,ShaderInfoLog:" + s);
        }
        return shader;
    }

    /**
     * @param fragment 顶点着色器文件
     * @param vertex   片段着色器文件
     * @return program
     */
    public static int createProgram(Context context, String fragment, String vertex) {
        int frag = loadShader(context, GLES30.GL_FRAGMENT_SHADER, fragment);
        int vert = loadShader(context, GLES30.GL_VERTEX_SHADER, vertex);
        int program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, frag);
        GLES30.glAttachShader(program, vert);
        GLES30.glLinkProgram(program);
        return program;
    }

    /**
     * @param fragment 顶点着色器文件
     * @param vertex   片段着色器文件
     * @return program
     */
    public static int createProgram(Context context, int fragment, int vertex) {
        int frag = loadShader(context, GLES30.GL_FRAGMENT_SHADER, fragment);
        int vert = loadShader(context, GLES30.GL_VERTEX_SHADER, vertex);
        int program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, frag);
        GLES30.glAttachShader(program, vert);
        GLES30.glLinkProgram(program);
        return program;
    }

    /**
     * 使用bitmap新建一个新纹理
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


    /**
     * 新建一个新纹理
     *
     * @param width  宽
     * @param height 高
     */
    public static int newTexture(int width, int height) {
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
        return texture;
    }

    private static String uRes(Resources res, int path) {
        StringBuilder result = new StringBuilder();
        InputStream is = null;
        try {
            is = res.openRawResource(path);
            int ch;
            byte[] buffer = new byte[1024];
            while (-1 != (ch = is.read(buffer))) {
                result.append(new String(buffer, 0, ch));
            }
        } catch (Exception e) {
            // nodo
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    // nodo
                }
        }
        return result.toString().replaceAll("\\r\\n", "\n");
    }

    private static String uRes(Resources mRes, String path) {
        StringBuilder result = new StringBuilder();
        InputStream is = null;
        try {
            is = mRes.getAssets().open(path);
            int ch;
            byte[] buffer = new byte[1024];
            while (-1 != (ch = is.read(buffer))) {
                result.append(new String(buffer, 0, ch));
            }
        } catch (Exception e) {
            // nodo
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // nodo
                }
            }
        }
        return result.toString().replaceAll("\\r\\n", "\n");
    }
}
