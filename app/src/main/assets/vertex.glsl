#version 300 es

layout(location = 0)in vec4 vPosition;
layout(location = 1)in vec4 vColor;

out vec4 gColor;

void main() {
    gl_Position = vPosition;
    gColor = vColor;
}
