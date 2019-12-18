cmake_minimum_required(VERSION 3.4.1)

find_path(GLES2_INCLUDE_DIR GLES3/gl3.h
        HINTS ${ANDROID_NDK})              #找到opengl头文件路径

add_library(
        GlLib SHARED GlLib.cpp
)

find_library(GLES3_LIBRARY libGLESv3.so
        HINTS ${GLES3_INCLUDE_DIR}/../lib) #找到opengl库文件路径

include_directories(
        ${GLES3_INCLUDE_DIR}               #把头文件路径添加进来
)

target_link_libraries(
        ${GLES3_LIBRARY}
        GlLib
)