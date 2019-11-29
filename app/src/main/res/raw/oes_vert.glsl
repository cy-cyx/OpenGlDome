#version 300 es

in vec4 vPosition;
in vec2 vTexcoord;

out vec2 vert_texcoord;

void main() {
    gl_Position = vPosition;
    vert_texcoord = vTexcoord;
}