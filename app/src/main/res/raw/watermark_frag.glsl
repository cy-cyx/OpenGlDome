#version 300 es

precision highp float;

in vec2 vert_texcoord;

uniform sampler2D uSourceImage;

out vec4 frag_Color;

void main() {
    frag_Color = texture(uSourceImage, vert_texcoord);
}