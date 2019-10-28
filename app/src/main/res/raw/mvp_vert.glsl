#version 300 es

in vec4 vPosition;
in vec4 vColor;

uniform mat4 uMatrix;

out vec4 vert_Color;

void main() {
    gl_Position = uMatrix * vPosition;
    vert_Color = vColor;
}