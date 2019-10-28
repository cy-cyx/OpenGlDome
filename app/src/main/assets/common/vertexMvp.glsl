#version 300 es

layout(location = 0)uniform mat4 uMatrix;
layout(location = 1)in vec4 vPosition;
layout(location = 2)in vec4 vColor;

out vec4 gColor;

void main() {
    gl_Position = uMatrix * vPosition;
    gColor = vColor;
}
