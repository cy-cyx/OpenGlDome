#version 300 es

in vec4 vPosition;
in vec4 vColor;

out vec4 vert_color;

void main() {
    gl_Position = vPosition;
    vert_color = vColor;
}