#version 300 es

precision mediump float;

in vec2 vert_texcoord;
out vec4 frag_Color;

uniform sampler2D uTexture;

void main() {
    frag_Color = texture(uTexture, vert_texcoord);
}