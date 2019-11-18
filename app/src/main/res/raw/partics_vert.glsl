#version 300 es

in vec4 vPosition;

void main() {
    gl_Position = vPosition;
    gl_PointSize = 10.0f;
}
