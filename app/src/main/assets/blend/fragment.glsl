#version 300 es

precision mediump float;

in vec2 texcoord;
out vec4 g_Color;

uniform sampler2D vTexture;

void main() {
    g_Color = texture(vTexture, texcoord);
}
