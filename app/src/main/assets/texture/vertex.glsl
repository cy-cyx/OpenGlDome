#version 300 es

layout(location = 5)in vec4 vPosition;
layout(location = 6)in vec2 vTexcoord;
layout(location = 7) uniform sampler2D vTexture;

out vec2 texcoord;

void main() {
    gl_Position = vPosition;
    texcoord = vTexcoord;
}
