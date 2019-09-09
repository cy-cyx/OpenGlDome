#version 300 es

layout(location = 0)in vec4 vColor;
layout(location = 1)in vec4 vPosition;

out vec4 v_Color;

uniform mat4 uMVPMatrix;

void main() {
    gl_Position = uMVPMatrix * vPosition;
    v_Color = vColor;
}
