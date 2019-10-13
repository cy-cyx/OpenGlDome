#version 300 es

layout(location = 0)in vec4 vPosition;
layout(location = 1)in vec2 vTexcoord;
layout(location = 2) uniform sampler2D vTexture;

out vec2 texcoord;

void main() {
    gl_Position = vPosition;
    texcoord = vTexcoord;
}
