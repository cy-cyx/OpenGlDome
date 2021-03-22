#version 300 es

in vec4 vPosition;
in vec2 vTexcoord;

out vec2 vert_texcoord;

uniform mat4 uMatrix;

void main() {
    gl_Position = uMatrix * vPosition;
    vert_texcoord = vTexcoord;
}