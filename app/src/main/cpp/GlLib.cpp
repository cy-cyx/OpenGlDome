#include <jni.h>
#include <GLES3/gl3.h>

extern "C" GLuint loadShader(GLenum type, GLchar **shape, GLint length) {
    GLuint resultShape = glCreateShader(type);
    glShaderSource(resultShape, 1, shape, &length);
    glCompileShader(resultShape);
    return resultShape;
}

extern "C" JNIEXPORT jint JNICALL Java_android_com_opengldome_ndk_Gllib_creatBaseProgram
        (JNIEnv *env, jclass clazz) {

    GLchar frag[] = "#version 300 es\nprecision highp float;\nin vec4 vert_color;\nout vec4 frag_color;\nvoid main(){\nfrag_color=vert_color;\n}";
    GLchar *frags[] = {frag};

    GLint fragLength = sizeof(frag);

    GLchar vert[] = "#version 300 es\nin vec4 vPosition;\nin vec4 vColor;\nout vec4 vert_color;\nvoid main(){\ngl_Position=vPosition;\nvert_color=vColor;\n}";
    GLchar *verts[] = {vert};
    GLint vertLength = sizeof(vert);

    GLuint fragShape = loadShader(GL_FRAGMENT_SHADER, frags, fragLength);
    GLuint vertShape = loadShader(GL_VERTEX_SHADER, verts, vertLength);

    GLuint program = glCreateProgram();
    glAttachShader(program, vertShape);
    glAttachShader(program, fragShape);
    glLinkProgram(program);

    return program;
}
