#version 300 es

layout(location = 2)in vec4 vPosition;
layout(location = 3)in vec2 vTexcoord;

out vec2 texcoord;

void main() {
    gl_Position = vPosition;
    texcoord = vTexcoord;
}