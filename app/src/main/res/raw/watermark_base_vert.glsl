#version 300 es

in vec4 vPosition;
in vec2 vTexcoord;

out vec2 vert_texcoord;

void main() {
    vert_texcoord = vTexcoord;
    gl_Position = vPosition;
}