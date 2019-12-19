cmake_minimum_required(VERSION 3.4.1)

add_library(
        GlLib SHARED GlLib.cpp
)

#find_library()

target_link_libraries(
        GlLib

        #        对于内置库以下三种方法通用
        #        -lGLESv3
        #        libGLESv3.so
        GLESv3
)